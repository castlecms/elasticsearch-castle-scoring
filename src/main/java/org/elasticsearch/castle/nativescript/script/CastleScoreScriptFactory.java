/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.elasticsearch.castle.nativescript.script;

import org.apache.lucene.search.Scorer;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.index.fielddata.ScriptDocValues;
import org.elasticsearch.script.AbstractSearchScript;
import org.elasticsearch.script.AbstractFloatSearchScript;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;
import org.elasticsearch.script.ScriptException;

import java.io.IOException;
import java.util.Map;

/**
 * Factory for the script that boosts score of a record based on a value of  the record's field.
 *
 * This native script demonstrates how to write native custom scores scripts.
 */
public class CastleScoreScriptFactory implements NativeScriptFactory {

    @Override
    public ExecutableScript newScript(@Nullable Map<String, Object> params) {
        String searchQ = params == null ? null : XContentMapValues.nodeStringValue(params.get("search"), null);
        if (searchQ == null) {
            throw new ScriptException("Missing the field parameter");
        }
        return new CastleScoreScript(searchQ);
    }

    /**
     * Indicates if document scores may be needed by the produced scripts.
     *
     * @return {@code true} if scores are needed.
     */
    @Override
    public boolean needsScores() {
        return true;
    }

    /**
     * This script takes a numeric value from the field specified in the parameter field. And calculates boost
     * for the record using the following formula: 1 + log10(field_value + 1). So, records with value 0 in the field
     * get no boost. Records with value 9 gets boost of 2.0, records with value 99, gets boost of 3, 999 - 4 and so on.
     */
    private static class CastleScoreScript extends AbstractFloatSearchScript {

        private final String searchQ;
        private Scorer scorer;
        private float maxPopularity = 200000.0f;
        private float maxShares = 5000.0f;
        private String[] socialFields = { "twitter", "facebook", "pinterist", "linkedin" };

        public CastleScoreScript(String searchQ) {
            this.searchQ = searchQ;
        }

        @Override
        public void setScorer(Scorer scorer) {
            this.scorer = scorer;
            super.setScorer(scorer);
        }

        @Override
        public float runAsFloat() {
            try {
                Map doc = doc();
                if(doc.containsKey("searchterm_pins")){
                    ScriptDocValues docValue = (ScriptDocValues) doc.get("searchterm_pins");
                    if (docValue != null && !docValue.isEmpty()) {
                        ScriptDocValues.Strings fieldData = (ScriptDocValues.Strings) docValue;
                        for(int i=0; i<fieldData.size(); i++){
                            String value = fieldData.get(i);
                            if(value.equalsIgnoreCase(searchQ)){
                                return 10000.0f;
                            }
                        }
                    }
                }

                float boost = 1.0f;
                long shareCount = 0;
                for(int i=0; i<this.socialFields.length; i++){
                    String key = this.socialFields[i] + "_shares";
                    if(doc.containsKey(key)){
                        ScriptDocValues docValue = (ScriptDocValues) doc.get(key);
                        if (docValue != null && !docValue.isEmpty() && docValue.size() > 0) {
                            shareCount += (long)docValue.get(0);
                        }
                    }
                }
                boost += (shareCount / this.maxShares);

                if(doc.containsKey("page_views")){
                    ScriptDocValues docValue = (ScriptDocValues) doc.get("page_views");
                    if (docValue != null && !docValue.isEmpty() && docValue.size() > 0) {
                        boost += ((long)docValue.get(0) / this.maxPopularity);
                    }
                }
                boost = Math.min(boost, 2.5f);

                return boost * this.scorer.score();
            } catch (IOException ex) {
                return 0.0f;
            }
        }
    }
}
