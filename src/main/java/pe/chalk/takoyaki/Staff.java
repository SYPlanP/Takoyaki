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

package pe.chalk.takoyaki;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import pe.chalk.takoyaki.logger.PrefixedLogger;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ChalkPE <chalkpe@gmail.com>
 * @since 2015-09-27
 */
public class Staff extends WebClient {
    private PrefixedLogger logger;
    private String encoding;

    public Staff(PrefixedLogger logger, int timeout){
        this(logger, timeout, null, null);
    }

    public Staff(PrefixedLogger logger, int timeout, String encoding){
        this(logger, timeout, encoding, null);
    }

    public Staff(PrefixedLogger logger, int timeout, JSONObject accountProperties){
        this(logger, timeout, null, accountProperties);
    }

    public Staff(PrefixedLogger logger, int timeout, String encoding, JSONObject accountProperties){
        super(BrowserVersion.CHROME);
        this.getOptions().setTimeout(timeout);
        this.getOptions().setJavaScriptEnabled(false);
        this.getOptions().setCssEnabled(false);

        this.encoding = encoding;
        this.logger = logger;

        Logger.getLogger("org.apache").setLevel(Level.OFF);
        Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

        if(accountProperties != null) this.login(accountProperties);
    }

    public void login(JSONObject accountProperties){
        this.logger.info("네이버에 로그인합니다: " + accountProperties.getString("username"));

        try{
            final HtmlPage loginPage = this.getPage("https://nid.naver.com/nidlogin.login?url=");
            final HtmlForm loginForm = loginPage.getFormByName("frmNIDLogin");

            final HtmlTextInput     idInput     = loginForm.getInputByName("id");
            final HtmlPasswordInput pwInput     = loginForm.getInputByName("pw");
            final HtmlSubmitInput   loginButton = (HtmlSubmitInput) loginForm.getByXPath("//fieldset/span/input").get(0);

            final String id = accountProperties.getString("username");
            final String pw = accountProperties.getString("password");

            if(id.equals("") || pw.equals("")){
                this.logger.notice("네이버 로그인 불가: 계정 정보가 설정되어 있지 않습니다");
                return;
            }

            idInput.setValueAttribute(id);
            pwInput.setValueAttribute(pw);

            Elements errors = Jsoup.parse(((HtmlPage) loginButton.click()).asXml().replaceFirst("<\\?xml version=\"1.0\" encoding=\"(.+)\"\\?>", "<!DOCTYPE html>")).select("div.error");
            if(!errors.isEmpty()) this.logger.warning("네이버 로그인 실패: " + errors.text());
        }catch(Exception e){
            this.logger.warning("네이버 로그인 실패: " + e.getClass().getName() + ": " + e.getMessage());
        }

        this.close();
    }

    public String parse(String url) throws IOException {
        return this.parse(new URL(url));
    }

    public String parse(URL url) throws IOException {
        WebResponse response = this.getPage(url).getWebResponse();
        this.close();

        return (this.encoding == null) ? response.getContentAsString() : response.getContentAsString(this.encoding);
    }
}
