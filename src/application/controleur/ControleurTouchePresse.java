package application.controleur;

import application.modele.Environnement;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

public class ControleurTouchePresse implements EventHandler<KeyEvent>{
	
	private Environnement env;

	
	public ControleurTouchePresse(Environnement env) {
		this.env = env;
	}

	@Override
	public void handle(KeyEvent event) {
		switch (event.getCode()) {
		case D:
			env.getJoueur().setClickD(true);
			break;
		case Q:
			env.getJoueur().setClickQ(true);
				break;
			case SPACE:
				env.getJoueur().saut();
				break;
			case AMPERSAND:
				env.getJoueur().getInventaire().changerCase(0);
				break;
			case QUOTEDBL:
				env.getJoueur().getInventaire().changerCase(2);
				break;
			case QUOTE:
				env.getJoueur().getInventaire().changerCase(3);
				break;
			case LEFT_PARENTHESIS:
				env.getJoueur().getInventaire().changerCase(4);
				break;
			case UNDEFINED:
				switch (event.getText()) {
					case "é":
						env.getJoueur().getInventaire().changerCase(1);
						break;
				}
				break;
			default:
				break; 
		}
	}
	

}