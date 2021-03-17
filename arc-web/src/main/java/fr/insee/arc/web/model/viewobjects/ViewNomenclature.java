package fr.insee.arc.web.model.viewobjects;

import java.util.HashMap;

import fr.insee.arc.web.util.ConstantVObject;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.VObject;

public class ViewNomenclature extends VObject {
	public ViewNomenclature() {
		super();
		
		this.setTitle("view.nomenclature");
		this.setDefaultPaginationSize(15);
		this.setSessionName("viewNomenclature");
        this.setConstantVObject(new ConstantVObject(new HashMap<String, ColumnRendering>() {
            /**
			 * 
			 */
            private static final long serialVersionUID = 595652512540034630L;

			{
                put("0", new ColumnRendering(true, "", "", "", null, true));
            }
        }

        ));
	}
}
