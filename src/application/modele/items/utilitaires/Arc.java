package application.modele.items.utilitaires;

public class Arc extends Utilitaire {
	public static int compteur = 1;
	public Arc(int mat) {
		super(mat, "A" + compteur);
		compteur++;
	}

}
