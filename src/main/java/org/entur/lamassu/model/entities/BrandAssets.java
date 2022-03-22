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

package org.entur.lamassu.model.entities;

import java.io.Serializable;

public class BrandAssets implements Serializable {
    private String brandLastModified;
    private String brandTermsUrl;
    private String brandImageUrl;
    private String brandImageUrlDark;
    private String color;

    public String getBrandLastModified() {
        return brandLastModified;
    }

    public void setBrandLastModified(String brandLastModified) {
        this.brandLastModified = brandLastModified;
    }

    public String getBrandTermsUrl() {
        return brandTermsUrl;
    }

    public void setBrandTermsUrl(String brandTermsUrl) {
        this.brandTermsUrl = brandTermsUrl;
    }

    public String getBrandImageUrl() {
        return brandImageUrl;
    }

    public void setBrandImageUrl(String brandImageUrl) {
        this.brandImageUrl = brandImageUrl;
    }

    public String getBrandImageUrlDark() {
        return brandImageUrlDark;
    }

    public void setBrandImageUrlDark(String brandImageUrlDark) {
        this.brandImageUrlDark = brandImageUrlDark;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "BrandAssets{" +
                "brandLastModified='" + brandLastModified + '\'' +
                ", brandTermsUrl='" + brandTermsUrl + '\'' +
                ", brandImageUrl='" + brandImageUrl + '\'' +
                ", brandImageUrlDark='" + brandImageUrlDark + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
