package application.controleur;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import application.modele.Environnement;
import application.modele.Fleche;
import application.modele.Joueur;
import application.modele.craft.EpeeCraft;
import application.modele.craft.OutilCraft;
import application.modele.craft.HacheCraft;
import application.modele.craft.PiocheCraft;
import application.modele.craft.materiaux.Materiaux;
import application.modele.mobs.Mob;
import application.modele.mobs.Slime;
import application.vue.CraftVue;
import application.vue.ImageMap;
import application.vue.InventaireVue;
import application.vue.JoueurVue;
import application.vue.MobVue;
import application.vue.PVVue;
import application.vue.TerrainVue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.animation.KeyFrame;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class Controleur implements Initializable{

	@FXML
	private Pane terrainPane;
	@FXML
	private Pane craftPane;
	@FXML
	private TilePane terrainMap;
	@FXML
	private ImageView spriteJoueur;
	@FXML
	private ImageView spriteSlime;
	@FXML
	private BorderPane root;
	@FXML
	private HBox pointsDeVie;
	@FXML
	private HBox inventaireAff;
	@FXML
	private HBox inventaireSelect;
	@FXML
	private HBox inventaireItems;
	@FXML
	private Text boisText;
	@FXML
	private Text ferText;
	@FXML
	private Text orText;
	@FXML
	private Text diamantText;

	private Environnement env;
	private Timeline gameLoop;
	private MobVue mobAffichage;


	@Override
	public void initialize(URL location, ResourceBundle resources) {

		//InitialiserImages
		ImageMap imgs = new ImageMap();

		//Création Terrain

		env = new Environnement();

		TerrainVue terrainVue = new TerrainVue(env, terrainMap);
		terrainVue.initTerrain();

		//Indices Terrain
		int pxl = 32;
		int longueur = 120;
		int hauteur = 33;
		terrainMap.setMaxSize(longueur*pxl , hauteur*pxl);

		//PV
		PVVue pvVue = new PVVue(pointsDeVie, env.getJoueur().getPvMax());
		pvVue.initPV();
		this.listenPV(pvVue);

		//Inventaire
		InventaireVue inventaire = new InventaireVue(inventaireAff, inventaireSelect, inventaireItems, env.getJoueur().getInventaire().taille()); 
		inventaire.initInventaire();
		this.listenInventaire(inventaire);
		this.listenInventaireCase(inventaire);

		//Matériaux
		ArrayList<Materiaux> comptMat = env.getJoueur().getCompteurMateriaux();
		boisText.textProperty().bind(Bindings.convert(comptMat.get(0).matProperty()));
		ferText.textProperty().bind(Bindings.convert(comptMat.get(1).matProperty()));
		orText.textProperty().bind(Bindings.convert(comptMat.get(2).matProperty()));
		diamantText.textProperty().bind(Bindings.convert(comptMat.get(3).matProperty()));



		//Mobs
		this.mobAffichage = new MobVue();
		this.env.getMobs().addListener(new MobsObsList(this));
		this.env.creerSlime();

		//Init Craft
		CraftVue craft = new CraftVue(terrainPane, craftPane);
		this.env.getJoueur().getCompteurMateriaux().get(0).ajouterMat(2);
		this.env.getJoueur().getCompteurMateriaux().get(1).ajouterMat(2);
		this.env.getJoueur().getCompteurMateriaux().get(2).ajouterMat(2);

		//Lancement Joueur
		this.bindJoueur();
		root.addEventHandler(KeyEvent.KEY_PRESSED, new ControleurTouchePresse(env, craft));
		root.addEventHandler(KeyEvent.KEY_RELEASED, new ControleurToucheRelache(env));
		root.addEventHandler(ScrollEvent.SCROLL, new ControleurScroll(env));

		//Lancement GameLoop
		initAnimation();
		gameLoop.play();

	}

	@FXML
	void ameliorationEpee(ActionEvent event) {
		this.env.getJoueur().getEpee().craft();
	}
	
	@FXML
    void ameliorationHache(ActionEvent event) {
		this.env.getJoueur().getHache().craft();
    }

    @FXML
    void ameliorationPioche(ActionEvent event) {
    	this.env.getJoueur().getPioche().craft();
    }

	public void listenPV(PVVue pvVue) {
		IntegerProperty pv = env.getJoueur().pvProperty();

		pv.addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				pvVue.changerPV((int) newValue);
			}
		});
	}

	public void listenInventaire(InventaireVue inv) {
		IntegerProperty curseur = env.getJoueur().getInventaire().indexProperty();
		inv.positionnerCurseur(curseur.get());

		curseur.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				inv.enleverCurseur((int) oldValue);
				inv.positionnerCurseur((int) newValue);
			}
		});

	}



	public void listenInventaireCase(InventaireVue inv) {
		IntegerProperty curseurCase = env.getJoueur().getInventaire().indexCaseProperty();
		curseurCase.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				String idItem = env.getJoueur().getInventaire().idItemEnMain();
				int curseur = env.getJoueur().getInventaire().getIndexProperty();
				inv.changerImage(curseur, idItem);
			}
		});
	}


	public void bindJoueur() {
		Joueur j = env.getJoueur();
		spriteJoueur.translateYProperty().bind(j.yProperty());
		listenJoueurProperty();
		new JoueurVue(j.xProperty(),j.yProperty(),spriteJoueur);

	}

	public void listenJoueurProperty() {
		env.getJoueur().xProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

				if ((double) newValue > 960 && (double) newValue < 2880)  {
					terrainPane.setTranslateX(-(double) newValue+960);
				}
				else if ((double) newValue <= 960 ) {
					spriteJoueur.setTranslateX((double) newValue-960);
				}
				else
					spriteJoueur.setTranslateX((double) newValue-2880);
			}
		});
	}


	//GESTION SPRITE

	public void enleverSprite (String id) {
		terrainPane.lookup("#" + id).setVisible(false);
		terrainPane.getChildren().remove(terrainPane.lookup("#" + id));

	}

	public void ajouterJoueur (ImageView sprite) {
		this.terrainMap.getChildren().add(sprite);
	}

	private void initAnimation() {
		gameLoop = new Timeline();
		gameLoop.setCycleCount(Timeline.INDEFINITE);

		KeyFrame kf = new KeyFrame(
				//FPS
				Duration.seconds(0.017), 
				// on définit ce qui se passe à chaque frame 
				// c'est un eventHandler d'ou le lambda
				(ev ->{

					env.unTour();

				})
				);
		gameLoop.getKeyFrames().add(kf);
	}

	public void creerSpriteMob(Mob m) {
		ImageView mobSprite = null;
		if (m instanceof Slime) {
			mobSprite = mobAffichage.creerSlime(m.getId());
			terrainPane.getChildren().add(mobSprite);
			mobSprite.translateXProperty().bind(m.xProperty());
			mobSprite.translateYProperty().bind(m.yProperty());
		}
		else if (m instanceof Fleche) {
			mobSprite = mobAffichage.creerFleche(m.getId());
			terrainPane.getChildren().add(mobSprite);
			mobSprite.translateXProperty().bind(m.xProperty());
			mobSprite.translateYProperty().bind(m.yProperty());
			if (!m.getJoueur().isVersDroite()) {
				mobSprite.setScaleX(-1);
			}

		}

	}

	public void supprimerSprite(Mob m) {
		if (m instanceof Slime) {

		}

	}
}

