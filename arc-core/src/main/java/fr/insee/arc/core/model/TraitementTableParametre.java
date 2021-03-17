package fr.insee.arc.core.model;

public enum TraitementTableParametre {
    CALENDRIER("CALENDRIER"), //
//    CLIENT("CLIENT"), //
//    FAMILLE("FAMILLE"), //
    NORME("NORME"), //
    JEUDEREGLE("JEUDEREGLE"), //
    CHARGEMENT_REGLE("CHARGEMENT_REGLE"), //
    NORMAGE_REGLE("NORMAGE_REGLE"), //
    CONTROLE_REGLE("CONTROLE_REGLE"), //
    MAPPING_REGLE("MAPPING_REGLE"), //
    FILTRAGE_REGLE("FILTRAGE_REGLE"), //
    EXPRESSION("EXPRESSION"), //
    SEUIL("SEUIL"), //
    MOD_TABLE_METIER("MOD_TABLE_METIER"), //
    MOD_VARIABLE_METIER("MOD_VARIABLE_METIER");
    private TraitementTableParametre(String anExpression) {
        this.expression = anExpression;
    }

    private String expression;
    
    public boolean isPartOfRuleset() {
    	return this == TraitementTableParametre.CHARGEMENT_REGLE 
        		|| this == TraitementTableParametre.NORMAGE_REGLE 
        		|| this == TraitementTableParametre.CONTROLE_REGLE
                || this == TraitementTableParametre.MAPPING_REGLE 
                || this == TraitementTableParametre.FILTRAGE_REGLE 
                || this == TraitementTableParametre.EXPRESSION;
    }

    @Override
    public String toString() {
        return this.expression;
    }
}
