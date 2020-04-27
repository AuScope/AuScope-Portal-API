package org.auscope.portal.server.web.controllers.sessonobject;

import org.auscope.portal.core.services.csw.custom.CustomRegistryInt;
import org.springframework.core.convert.converter.Converter;

public class StringArrayToCustomRegistry implements Converter<String[], CustomRegistryInt> {

    @Override
    public CustomRegistryInt convert(String[] source) {
        return new CustomRegistry(source);
    }

    private final class CustomRegistry implements CustomRegistryInt {
        private String id;
        private String title, serviceUrl;
        private String recordInformationUrl;

        public CustomRegistry(String id, String title, String serviceUrl, String recordInformationUrl) {
            this.setId(id);
            this.setTitle(title);
            if (!serviceUrl.endsWith("csw")) {
                this.setServiceUrl(serviceUrl + "/csw");
            } else {
                this.setServiceUrl(serviceUrl);
            }

            if (!recordInformationUrl.endsWith("uuid=%1$s")) {
                this.setRecordInformationUrl(recordInformationUrl + "?uuid=%1$s");
            } else {
                this.setRecordInformationUrl(recordInformationUrl);
            }
        }

        public CustomRegistry(String[] registryInfo) {
            this(registryInfo[0], registryInfo[1], registryInfo[2], registryInfo[3]);
        }

        @Override
        public boolean isEmpty() {
            //VT: All info are crucial therefore we don't recognize this registry if it is missing any information.
            if (id.isEmpty() || title.isEmpty() || serviceUrl.isEmpty() || recordInformationUrl.isEmpty()) {
                return true;
            } else {
                return false;
            }

        }

        /**
         * @return the recordInformationUrl
         */
        public String getRecordInformationUrl() {
            return recordInformationUrl;
        }

        /**
         * @param recordInformationUrl
         *            the recordInformationUrl to set
         */
        public void setRecordInformationUrl(String recordInformationUrl) {
            this.recordInformationUrl = recordInformationUrl;
        }

        /**
         * @return the serviceUrl
         */
        public String getServiceUrl() {
            return serviceUrl;
        }

        /**
         * @param serviceUrl
         *            the serviceUrl to set
         */
        public void setServiceUrl(String serviceUrl) {
            this.serviceUrl = serviceUrl;
        }

        /**
         * @return the title
         */
        public String getTitle() {
            return title;
        }

        /**
         * @param title
         *            the title to set
         */
        public void setTitle(String title) {
            this.title = title;
        }

        /**
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * @param id
         *            the id to set
         */
        public void setId(String id) {
            this.id = id;
        }

    }

}
