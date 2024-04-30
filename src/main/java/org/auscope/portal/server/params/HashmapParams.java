package org.auscope.portal.server.params;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


/**
 * Simple class that stores hashmap(key, value) etc. ROI
 * @author jia020
 *
 */
@Entity
@Table(name = "hashmap_params")
public class HashmapParams  implements Serializable {	
   
	private static final long serialVersionUID = 8620093753366974709L;
	 /** The primary key for this Params*/
    @Id
	@Column(nullable=false)
	private String key;
	/** service id of the dataset */
	@Column(length = 1000000,nullable=false)
    private String value;

    public HashmapParams() {
    	super();
    }
    
    public HashmapParams(String key, String value) {
        super();
        this.setKey(key);
        this.setValue(value);
    }
   /**
    * get key
    * @return
    */
   public String getKey( ) {
       return this.key;
   }

   /**
    * set key 
    * @param id
    */
   public void setKey(String key) {
       this.key = key;
   }

   /**
    * Get value 
    * @return
    */    
    public String getValue() {
		return value;
	}
   /**
    * Set value 
    * @return
    */ 
    public void setValue(String value) {
    	this.value = value;
    }
}


