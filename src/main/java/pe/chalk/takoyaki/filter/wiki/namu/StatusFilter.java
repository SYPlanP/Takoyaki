/*
 * Copyright 2014-2015 ChalkPE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pe.chalk.takoyaki.filter.wiki.namu;

import org.json.JSONArray;
import org.json.JSONObject;
import pe.chalk.takoyaki.filter.Filter;
import pe.chalk.takoyaki.model.wiki.namu.Document;
import pe.chalk.takoyaki.target.Target;
import pe.chalk.takoyaki.utils.Utils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ChalkPE <chalkpe@gmail.com>
 * @since 2015-10-31
 */
public class StatusFilter extends Filter<JSONArray, Document> {
    public static final String NAME = "wiki.namu.status";

    public StatusFilter(Target<JSONArray> target){
        super(target);
    }

    @Override
    public String getName(){
        return NAME;
    }

    @Override
    public List<Document> filter(JSONArray array){
        return Utils.buildStream(JSONObject.class, array).map(doc -> Document.create(this.getTarget(), doc)).collect(Collectors.toList());
    }

    @Override
    public String getPrefix(){
        return "최근 변경";
    }
}
