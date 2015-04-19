/*
 * Copyright 2015 ChalkPE
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

package pe.chalk.takoyaki.data;

import pe.chalk.takoyaki.Target;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * @author ChalkPE <amato0617@gmail.com>
 * @since 2015-04-07
 */
public class Member extends Data {
    private static final String DEFAULT_DISPLAY_ID = "********";

    private String id;
    private String name;

    public Member(Target target, String id, String name){
        super(target);

        this.id = id == null ? "" : id;
        this.name = name;
    }

    public String getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public InternetAddress getInternetAddress(){
        try{
            return new InternetAddress(this.getId() + "@naver.com");
        }catch(AddressException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString(){
        return this.getName() + " (" + (this.getId().length() > 0 ? this.getId() : DEFAULT_DISPLAY_ID) + ")";
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof Member && this.getId().equalsIgnoreCase(((Member) obj).getId());
    }
}
