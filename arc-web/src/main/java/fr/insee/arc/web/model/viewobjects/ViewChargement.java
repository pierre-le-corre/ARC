package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;

public class ViewChargement extends AbstractViewRule {
    public ViewChargement() {
        super("view.load", "viewChargement", 15);        
    }
    
    @Override
    protected Map<String, ColumnRendering> extraFields() {
    	HashMap<String, ColumnRendering> columnRenderings = new HashMap<>();
        columnRenderings.put("type_fichier", new ColumnRendering(true, "label.file.type", "10%", "select",
        		new PreparedStatementBuilder("select id, id from arc.ext_type_fichier_chargement order by ordre"), true));
        columnRenderings.put("delimiter", new ColumnRendering(true, "label.file.delimiter", "10%", "text", null, true));
        columnRenderings.put("format", new ColumnRendering(true, "label.file.format", "65%", "text", null, true));
        columnRenderings.put("commentaire", new ColumnRendering(true, "label.comment", "10%", "text", null, true));
        return columnRenderings;
    }
}