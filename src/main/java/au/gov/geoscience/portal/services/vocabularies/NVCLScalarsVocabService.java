package au.gov.geoscience.portal.services.vocabularies;

import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.VocabularyService;
import org.auscope.portal.core.services.methodmakers.VocabularyMethodMaker;

/**
 * A vocab service used to retrieve vocab information concerning NVCL Scalars
 */
public class NVCLScalarsVocabService extends VocabularyService {

	public NVCLScalarsVocabService(HttpServiceCaller httpServiceCaller, VocabularyMethodMaker vocabularyMethodMaker,
			String serviceUrl) {
		super(httpServiceCaller, vocabularyMethodMaker, serviceUrl );

	}

}
