package org.auscope.portal.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;

import org.auscope.portal.core.services.vocabs.VocabularyServiceItem;
import org.auscope.portal.server.web.controllers.VocabController;
import org.auscope.portal.core.services.methodmakers.VocabularyMethodMaker;
import org.auscope.portal.core.server.http.HttpServiceCaller;

import au.gov.geoscience.portal.services.vocabularies.GeologicTimescaleVocabService;
import au.gov.geoscience.portal.services.vocabularies.CommodityVocabService;
import au.gov.geoscience.portal.services.vocabularies.MineStatusVocabService;
import au.gov.geoscience.portal.services.vocabularies.ResourceCategoryVocabService;
import au.gov.geoscience.portal.services.vocabularies.ReserveCategoryVocabService;
import au.gov.geoscience.portal.services.vocabularies.NVCLScalarsVocabService;


@Configuration
class ExtraVocabularies {

    @Lazy
    @Autowired
    HttpServiceCaller httpServiceCaller;
    
    @Bean
    public GeologicTimescaleVocabService geologicTimescaleService() {
        return new GeologicTimescaleVocabService(httpServiceCaller, new VocabularyMethodMaker(), "http://vocabs.ardc.edu.au/repository/api/lda/csiro/international-chronostratigraphic-chart/2017");
    }
    @Bean
    public VocabularyServiceItem vocabularyGeologicTimescales() {
        return new VocabularyServiceItem(VocabController.TIMESCALE_VOCABULARY_ID, "Geological Timescales Vocabulary", geologicTimescaleService());
    }

    @Bean
    public CommodityVocabService commodityCodeService() {
        return new CommodityVocabService(httpServiceCaller, new VocabularyMethodMaker(), "https://vocabs.ardc.edu.au/repository/api/lda/ga/commodity-code/v0-2");
    }
    @Bean
    public VocabularyServiceItem vocabularyCommodities() {
        return new VocabularyServiceItem(VocabController.COMMODITY_VOCABULARY_ID, "Commodities Vocabulary", commodityCodeService());
    }

    @Bean
    public MineStatusVocabService mineStatusService() {
        return new MineStatusVocabService(httpServiceCaller, new VocabularyMethodMaker(), "https://vocabs.ardc.edu.au/repository/api/lda/ga/mine-status/v0-1");
    }
    @Bean
    public VocabularyServiceItem vocabularyMineStatuses() {
        return new VocabularyServiceItem(VocabController.MINE_STATUS_VOCABULARY_ID, "Mine Statuses Vocabulary", mineStatusService());
    }

    @Bean
    public ResourceCategoryVocabService resourceCategoryService() {
        return new ResourceCategoryVocabService(httpServiceCaller, new VocabularyMethodMaker(), "https://vocabs.ardc.edu.au/repository/api/lda/ga/resource-assessment-category/v0-1");
    }
    @Bean
    public VocabularyServiceItem vocabularyResourceCategories() {
        return new VocabularyServiceItem(VocabController.RESOURCE_VOCABULARY_ID, "Resource Categories Vocabulary", resourceCategoryService());
    }

    @Bean
    public ReserveCategoryVocabService reserveCategoryService() {
        return new ReserveCategoryVocabService(httpServiceCaller, new VocabularyMethodMaker(), "https://vocabs.ardc.edu.au/repository/api/lda/ga/reserve-assessment-category/v0-1");
    }
    @Bean
    public VocabularyServiceItem vocabularyReserveCategories() {
        return new VocabularyServiceItem(VocabController.RESERVE_VOCABULARY_ID, "Reserve Categories Vocabulary", reserveCategoryService());
    }

    @Bean
    public NVCLScalarsVocabService nvclScalarsVocabService() {
        return new NVCLScalarsVocabService(httpServiceCaller, new VocabularyMethodMaker(), "http://vocabs.ardc.edu.au/repository/api/lda/csiro/national-virtual-core-library-scalars/v0-3");
    }
    @Bean
    public VocabularyServiceItem nvclScalarsCategories() {
        return new VocabularyServiceItem(VocabController.NVCL_SCALARS_VOCABULARY_ID, "NVCL Scalars Vocabulary", nvclScalarsVocabService());
    }

}
