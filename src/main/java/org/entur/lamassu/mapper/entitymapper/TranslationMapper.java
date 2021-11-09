/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

package org.entur.lamassu.mapper.entitymapper;

import org.entur.lamassu.model.entities.TranslatedString;
import org.entur.lamassu.model.entities.Translation;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TranslationMapper {
    public TranslatedString mapSingleTranslation(String language, String value) {
        if (language == null || value == null) {
            return null;
        }
        var translation = mapTranslation(language, value);
        return mapTranslatedString(List.of(translation));
    }

    public TranslatedString mapTranslatedString(List<Translation> translations) {
        var translatedString = new TranslatedString();
        translatedString.setTranslation(translations);
        return translatedString;
    }

    public Translation mapTranslation(String language, String value) {
        var translation = new Translation();
        translation.setLanguage(language);
        translation.setValue(value);
        return translation;
    }
}
