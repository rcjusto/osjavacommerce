package org.store.core.globals;

import com.octo.captcha.component.image.backgroundgenerator.BackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.FileReaderRandomBackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.UniColorBackgroundGenerator;
import com.octo.captcha.component.image.color.RandomRangeColorGenerator;
import com.octo.captcha.component.image.fontgenerator.FontGenerator;
import com.octo.captcha.component.image.fontgenerator.RandomFontGenerator;
import com.octo.captcha.component.image.textpaster.RandomTextPaster;
import com.octo.captcha.component.image.textpaster.TextPaster;
import com.octo.captcha.component.image.wordtoimage.ComposedWordToImage;
import com.octo.captcha.component.image.wordtoimage.WordToImage;
import com.octo.captcha.component.word.wordgenerator.RandomWordGenerator;
import com.octo.captcha.component.word.wordgenerator.WordGenerator;
import com.octo.captcha.engine.image.ListImageCaptchaEngine;
import com.octo.captcha.image.gimpy.GimpyFactory;
import com.octo.captcha.service.captchastore.FastHashMapCaptchaStore;
import com.octo.captcha.service.image.DefaultManageableImageCaptchaService;
import com.octo.captcha.service.image.ImageCaptchaService;
import org.apache.struts2.ServletActionContext;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

public class CaptchaServiceSingleton {

    private static Map<String,ImageCaptchaService> instance = new HashMap<String, ImageCaptchaService>();


    public static ImageCaptchaService getInstance(BaseAction action) {
        if (!instance.containsKey(action.getStoreCode())) {
            ImageCaptchaService is = new DefaultManageableImageCaptchaService(
                    new FastHashMapCaptchaStore(), new MyImageCaptchaEngine(),
                    180,
                    100000,
                    75000);
            instance.put(action.getStoreCode(), is);
        }
        return instance.get(action.getStoreCode());
    }


    public static class MyImageCaptchaEngine extends ListImageCaptchaEngine {

        protected void buildInitialFactories() {

            BaseAction action = (ServletActionContext.getContext().getActionInvocation().getAction() instanceof BaseAction) ? (BaseAction) ServletActionContext.getContext().getActionInvocation().getAction() : null;
            String path = "captchas";
            if (action!=null) {
                path = action.getServletContext().getRealPath("/stores/" + action.getStoreCode() + "/captchas");
            }

            WordGenerator wgen = new RandomWordGenerator("ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789");
            RandomRangeColorGenerator cgen = new RandomRangeColorGenerator(
                    new int[]{0, 100},
                    new int[]{0, 100},
                    new int[]{0, 100});
            TextPaster textPaster = new RandomTextPaster(7, 7, cgen, true);

         //   BackgroundGenerator backgroundGenerator = new FunkyBackgroundGenerator(200, 60);
            BackgroundGenerator backgroundGenerator;
            try {
                backgroundGenerator = new FileReaderRandomBackgroundGenerator(200, 60, path);
            } catch (Exception e) {
                backgroundGenerator = new UniColorBackgroundGenerator(200, 60, Color.WHITE);
            }

            Font[] fontsList = new Font[]{
                    new Font("Arial", 0, 10),
                    new Font("Tahoma", 0, 10),
                    new Font("Verdana", 0, 10),
            };

            FontGenerator fontGenerator = new RandomFontGenerator(20, 35, fontsList);

            WordToImage wordToImage = new ComposedWordToImage(fontGenerator, backgroundGenerator, textPaster);
            this.addFactory(new GimpyFactory(wgen, wordToImage));
        }
    }


}
