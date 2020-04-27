package org.auscope.portal.server.web.service;

/**
 * A highly simplified representation of an XSD Element describing a property
 * @author Josh Vote
 *
 */
public class SimpleFeatureProperty {
        /** xsd:element maxOccurs attribute */
        private int maxOccurs;
        /** xsd:element minOccurs attribute */
        private int minOccurs;
        /** xsd:element name attribute */
        private String name;
        /** xsd:element nillable attribute */
        private boolean nillable;
        /** xsd:element type attribute */
        private String typeName;
        /** The sequence index of this element - This index is 1 based*/
        private int index;
        
        /**
         * 
         * @param maxOccurs xsd:element maxOccurs attribute
         * @param minOccurs xsd:element minOccurs attribute
         * @param name xsd:element name attribute
         * @param nillable xsd:element nillable attribute
         * @param typeName xsd:element type attribute
         * @param index The sequence index of this element - This index is 1 based
         */
        public SimpleFeatureProperty(int maxOccurs, int minOccurs, String name,
                boolean nillable, String typeName, int index) {
            super();
            this.maxOccurs = maxOccurs;
            this.minOccurs = minOccurs;
            this.name = name;
            this.nillable = nillable;
            this.typeName = typeName;
            this.index = index;
        }
        
        /**
         * xsd:element maxOccurs attribute
         * @return
         */
        public int getMaxOccurs() {
            return maxOccurs;
        }
        
        /**
         * xsd:element maxOccurs attribute
         * @param maxOccurs
         */
        public void setMaxOccurs(int maxOccurs) {
            this.maxOccurs = maxOccurs;
        }
        
        /**
         * xsd:element minOccurs attribute
         * @return
         */
        public int getMinOccurs() {
            return minOccurs;
        }
        
        /**
         * xsd:element minOccurs attribute
         * @param minOccurs
         */
        public void setMinOccurs(int minOccurs) {
            this.minOccurs = minOccurs;
        }
        
        /**
         * xsd:element name attribute
         * @return
         */
        public String getName() {
            return name;
        }
        
        /**
         * xsd:element name attribute
         * @param name
         */
        public void setName(String name) {
            this.name = name;
        }
        
        /**
         * xsd:element nillable attribute
         * @return
         */
        public boolean isNillable() {
            return nillable;
        }
        
        /**
         * xsd:element nillable attribute
         * @param nillable
         */
        public void setNillable(boolean nillable) {
            this.nillable = nillable;
        }
        
        /**
         * xsd:element type attribute
         * @return
         */
        public String getTypeName() {
            return typeName;
        }
        
        /**
         * xsd:element type attribute
         * @param typeName
         */
        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        /**
         * The sequence index of this element - This index is 1 based
         * @return
         */
        public int getIndex() {
            return index;
        }

        /**
         * The sequence index of this element - This index is 1 based
         * @param index
         */
        public void setIndex(int index) {
            this.index = index;
        }
        
        
}
