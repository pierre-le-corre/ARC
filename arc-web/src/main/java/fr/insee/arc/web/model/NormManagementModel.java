package fr.insee.arc.web.model;

import fr.insee.arc.web.model.viewobjects.ViewCalendrier;
import fr.insee.arc.web.model.viewobjects.ViewChargement;
import fr.insee.arc.web.model.viewobjects.ViewControle;
import fr.insee.arc.web.model.viewobjects.ViewExpression;
import fr.insee.arc.web.model.viewobjects.ViewFiltrage;
import fr.insee.arc.web.model.viewobjects.ViewJeuxDeRegles;
import fr.insee.arc.web.model.viewobjects.ViewJeuxDeReglesCopie;
import fr.insee.arc.web.model.viewobjects.ViewMapping;
import fr.insee.arc.web.model.viewobjects.ViewNormage;
import fr.insee.arc.web.model.viewobjects.ViewNorme;
import fr.insee.arc.web.util.VObject;

public class NormManagementModel implements ArcModel {

	// The norm view
	private VObject viewNorme;

	// The calendar view
	private VObject viewCalendrier;

	// The ruleset view
	private VObject viewJeuxDeRegles;

	// The load rules view
	private VObject viewChargement;

	// The structurize rules view
	private VObject viewNormage;

	// The control rules view
	private VObject viewControle;

	// The filter rules view
	private VObject viewFiltrage;

	// The map to format rules view
	private VObject viewMapping;
	
	// Expression to use in mapping
	private VObject viewExpression;

	// The on ruleset to copy rules
	private VObject viewJeuxDeReglesCopie;

	public NormManagementModel() {
		this.viewNorme = new ViewNorme();
		this.viewCalendrier = new ViewCalendrier();
		this.viewJeuxDeRegles = new ViewJeuxDeRegles();
		this.viewChargement = new ViewChargement();
		this.viewNormage = new ViewNormage();
		this.viewControle = new ViewControle();
		this.viewFiltrage = new ViewFiltrage();
		this.viewMapping = new ViewMapping();
		this.viewExpression = new ViewExpression();
		this.viewJeuxDeReglesCopie = new ViewJeuxDeReglesCopie();
	}
	
	public VObject getViewNorme() {
		return viewNorme;
	}

	public void setViewNorme(VObject viewNorme) {
		this.viewNorme = viewNorme;
	}

	public VObject getViewCalendrier() {
		return viewCalendrier;
	}

	public void setViewCalendrier(VObject viewCalendar) {
		this.viewCalendrier = viewCalendar;
	}
	public VObject getViewJeuxDeRegles() {
		return viewJeuxDeRegles;
	}

	public void setViewJeuxDeRegles(VObject viewRulesSet) {
		this.viewJeuxDeRegles = viewRulesSet;
	}

	public VObject getViewChargement() {
		return viewChargement;
	}

	public void setViewChargement(VObject viewChargement) {
		this.viewChargement = viewChargement;
	}

	public VObject getViewNormage() {
		return viewNormage;
	}

	public void setViewNormage(VObject viewNormage) {
		this.viewNormage = viewNormage;
	}

	public VObject getViewControle() {
		return viewControle;
	}

	public void setViewControle(VObject viewControle) {
		this.viewControle = viewControle;
	}

	public VObject getViewFiltrage() {
		return viewFiltrage;
	}

	public void setViewFiltrage(VObject viewFiltrage) {
		this.viewFiltrage = viewFiltrage;
	}

	public VObject getViewMapping() {
		return viewMapping;
	}

	public void setViewMapping(VObject viewMapping) {
		this.viewMapping = viewMapping;
	}
	
	public VObject getViewExpression() {
		return viewExpression;
	}

	public void setViewExpression(VObject viewExpression) {
		this.viewExpression = viewExpression;
	}

	public VObject getViewJeuxDeReglesCopie() {
		return viewJeuxDeReglesCopie;
	}

	public void setViewJeuxDeReglesCopie(VObject viewJeuxDeReglesCopie) {
		this.viewJeuxDeReglesCopie = viewJeuxDeReglesCopie;
	}

}