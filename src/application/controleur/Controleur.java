package application.controleur;

import java.net.URL;
import java.util.ResourceBundle;

import application.modele.Environnement;
import application.modele.Joueur;
import application.modele.Materiaux;
import application.modele.Mob;
import application.modele.Personnage;
import application.modele.Slime;
import application.vue.InventaireVue;
import application.vue.JoueurVue;
import application.vue.PVVue;
import application.vue.TerrainVue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.util.Duration;

public class Controleur implements Initializable{

	@FXML
    private Pane terrainPane;
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
    private javafx.scene.text.Text ferText;
    @FXML
    private javafx.scene.text.Text orText;
    @FXML
    private javafx.scene.text.Text diamantText;

	private Environnement env;
	private Timeline gameLoop;
	private int temps;


	@Override
	public void initialize(URL location, ResourceBundle resources) {

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
		Materiaux mat = new Materiaux();
		listenDiamant(mat);
		listenOr(mat);
		listenFer(mat);

		//Lancement Joueur
		this.bindJoueur();
		root.addEventHandler(KeyEvent.KEY_PRESSED, new ControleurTouchePresse(env));
		root.addEventHandler(KeyEvent.KEY_RELEASED, new ControleurToucheRelache(env));
		root.addEventHandler(ScrollEvent.SCROLL, new ControleurScroll(env));

		//LancementPersonnage
		this.bindSlime();

		//Lancement GameLoop
		initAnimation();
		gameLoop.play();

	}

	public void listenDiamant (Materiaux mat) {
		IntegerProperty diamant = mat.diamantProperty();
		diamant.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				diamantText.setText("" + newValue);
			}
		});
	}
	public void listenOr (Materiaux mat) {
		IntegerProperty or = mat.orProperty();
		or.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				orText.setText("" + newValue);
			}
		});
	}
	public void listenFer (Materiaux mat) {
		IntegerProperty fer = mat.ferProperty();
		fer.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				ferText.setText("" + newValue);
			}
		});
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
		DoubleProperty xCoord = env.getJoueur().xProperty();
		xCoord.addListener(new ChangeListener<Number>() {

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

	//Temporaire
	public void bindSlime() {
		for (Mob m : env.getMobs()) {
			if (m instanceof Slime) {
				spriteSlime.translateXProperty().bind(m.xProperty());
				spriteSlime.translateYProperty().bind(m.yProperty());
				
				m.pvProperty().addListener(new ChangeListener<Number>() {

					@Override
					public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
						if ((int)newValue==0) {
							spriteSlime.setImage(null);
							terrainMap.getChildren().remove(spriteSlime);
							env.retirerMob(m);
						}
					}	
				});
			}
		}
	}

	private void initAnimation() {
		gameLoop = new Timeline();
		temps=0;
		gameLoop.setCycleCount(Timeline.INDEFINITE);
		Personnage joueur=env.getJoueur();

		KeyFrame kf = new KeyFrame(
				//FPS
				Duration.seconds(0.017), 
				// on définit ce qui se passe à chaque frame 
				// c'est un eventHandler d'ou le lambda
				(ev ->{
					
					env.unTour();
						
					temps++;
				})
				);
		gameLoop.getKeyFrames().add(kf);
	}
}

