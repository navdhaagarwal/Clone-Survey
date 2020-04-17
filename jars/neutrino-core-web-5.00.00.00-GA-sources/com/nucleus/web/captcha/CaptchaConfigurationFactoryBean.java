package com.nucleus.web.captcha;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;

import com.nucleus.core.exceptions.SystemException;
import com.octo.captcha.CaptchaFactory;
import com.octo.captcha.component.image.backgroundgenerator.AbstractBackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.GradientBackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.MultipleShapeBackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.UniColorBackgroundGenerator;
import com.octo.captcha.component.image.color.RandomListColorGenerator;
import com.octo.captcha.component.image.fontgenerator.RandomFontGenerator;
import com.octo.captcha.component.image.textpaster.SimpleTextPaster;
import com.octo.captcha.component.image.wordtoimage.ComposedWordToImage;
import com.octo.captcha.engine.GenericCaptchaEngine;
import com.octo.captcha.image.gimpy.GimpyFactory;
import com.octo.captcha.service.multitype.GenericManageableCaptchaService;

public class CaptchaConfigurationFactoryBean implements FactoryBean<GenericManageableCaptchaService>, InitializingBean {

    private static final int                DEFAULT_CAPTCHA_COMPLEXITY       = 1;

    private static final int                DEFAULT_MIN_ACCEPTED_WORD_LENGTH = 1;
    private static final int                DEFAULT_MAX_ACCEPTED_WORD_LENGTH = 1;

    private static final String[]           DEFAULT_FONT_LIST                = { "DroidSerif.ttf" };

    private static final int                DEFAULT_BACKGROUND_WIDTH         = 325;
    private static final int                DEFAULT_BACKGROUND_HEIGHT        = 95;

    private static final int                DEFAULT_MIN_FONT_SIZE            = 50;
    private static final int                DEFAULT_MAX_FONT_SIZE            = 65;

    private String                          charSet;

    private Color                           colorPink                        = new Color(122, 178, 106);
    private Color                           colorOrange                      = new Color(255, 153, 0);
    private Color                           colorGreen                       = new Color(0, 235, 0);
    private Color                           colorMagneta                     = new Color(255, 0, 255);
    private Color                           colorBlue                        = new Color(242, 44, 118);
    private Color                           colorWhite                       = new Color(255, 255, 255);
    private Color                           colorGray                        = new Color(227,227,227);

    private int                             minAcceptedWordLength            = DEFAULT_MIN_ACCEPTED_WORD_LENGTH;
    private int                             maxAcceptedWordLength            = DEFAULT_MAX_ACCEPTED_WORD_LENGTH;

    private int                             captchaComplexity                = DEFAULT_CAPTCHA_COMPLEXITY;

    private RandomListColorGenerator        textColorGenerator               = new RandomListColorGenerator(new Color[] {
            colorBlue, colorGreen, colorMagneta, colorOrange, colorPink     });

    private SimpleTextPaster                simpleWhitePaster                = new SimpleTextPaster(minAcceptedWordLength,
                                                                                     maxAcceptedWordLength,
                                                                                     textColorGenerator, true);

    private AbstractBackgroundGenerator     backgroundGenerator              = new UniColorBackgroundGenerator(
                                                                                     DEFAULT_BACKGROUND_WIDTH,
                                                                                     DEFAULT_BACKGROUND_HEIGHT, colorWhite);
    private List<Font>                      fontList                         = createFonts();
    private GenericManageableCaptchaService genericManageableCaptchaService;

    @Override
    public GenericManageableCaptchaService getObject() throws Exception {

        return genericManageableCaptchaService;
    }

    private List<Font> createFonts() {

        fontList = new ArrayList<Font>();

        for (int i = 0 ; i < DEFAULT_FONT_LIST.length ; i++) {
            ClassPathResource resource = new ClassPathResource("neutrino-web-static/fonts/" + DEFAULT_FONT_LIST[i]);
            try {
                fontList.add(Font.createFont(0, resource.getInputStream()));
            } catch (FontFormatException e) {
                throw new SystemException("Invalid font Format.", e);
            } catch (IOException e) {
                throw new SystemException("IO Exception While creating font.", e);
            }
        }
        return fontList;
    }

    @Override
    public Class<GenericManageableCaptchaService> getObjectType() {
        return GenericManageableCaptchaService.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setCharset(String charSet) {
        this.charSet = charSet;
    }

    public void setCaptchaComplexity(int captchaComplexity) {
        this.captchaComplexity = captchaComplexity;
    }

    @Override
    public void afterPropertiesSet() throws Exception {



        switch (captchaComplexity > 8 ? 8 : captchaComplexity) {
            case 2:
                simpleWhitePaster = new SimpleTextPaster(2, 2, textColorGenerator, true);
              //backgroundGenerator = new MultipleShapeBackgroundGenerator(DEFAULT_BACKGROUND_WIDTH, DEFAULT_BACKGROUND_HEIGHT);
                backgroundGenerator = new GradientBackgroundGenerator(DEFAULT_BACKGROUND_WIDTH, DEFAULT_BACKGROUND_HEIGHT, colorGray,colorWhite);
                break;
            case 3:
                simpleWhitePaster = new SimpleTextPaster(3, 3, textColorGenerator, true);
                //backgroundGenerator = new MultipleShapeBackgroundGenerator(DEFAULT_BACKGROUND_WIDTH, DEFAULT_BACKGROUND_HEIGHT);
                backgroundGenerator = new GradientBackgroundGenerator(DEFAULT_BACKGROUND_WIDTH, DEFAULT_BACKGROUND_HEIGHT, colorGray,colorWhite);

                break;
            case 4:
                simpleWhitePaster = new SimpleTextPaster(4, 4, textColorGenerator, true);
              //backgroundGenerator = new MultipleShapeBackgroundGenerator(DEFAULT_BACKGROUND_WIDTH, DEFAULT_BACKGROUND_HEIGHT);
                backgroundGenerator = new GradientBackgroundGenerator(DEFAULT_BACKGROUND_WIDTH, DEFAULT_BACKGROUND_HEIGHT, colorGray,colorWhite);

                break;
            case 5:
                simpleWhitePaster = new SimpleTextPaster(5, 5, textColorGenerator, true);
              //backgroundGenerator = new MultipleShapeBackgroundGenerator(DEFAULT_BACKGROUND_WIDTH, DEFAULT_BACKGROUND_HEIGHT);
                backgroundGenerator = new GradientBackgroundGenerator(DEFAULT_BACKGROUND_WIDTH, DEFAULT_BACKGROUND_HEIGHT, colorGray,colorWhite);
                break;
            case 6:
                simpleWhitePaster = new SimpleTextPaster(6, 6, textColorGenerator, true);
                //backgroundGenerator = new MultipleShapeBackgroundGenerator(DEFAULT_BACKGROUND_WIDTH, DEFAULT_BACKGROUND_HEIGHT);
                backgroundGenerator = new GradientBackgroundGenerator(DEFAULT_BACKGROUND_WIDTH, DEFAULT_BACKGROUND_HEIGHT, colorGray,colorWhite);

                break;
            case 7:
                simpleWhitePaster = new SimpleTextPaster(7, 7, textColorGenerator, true);
              //backgroundGenerator = new MultipleShapeBackgroundGenerator(DEFAULT_BACKGROUND_WIDTH, DEFAULT_BACKGROUND_HEIGHT);
                backgroundGenerator = new GradientBackgroundGenerator(DEFAULT_BACKGROUND_WIDTH, DEFAULT_BACKGROUND_HEIGHT, colorGray,colorWhite);

                break;
            case 8:
                simpleWhitePaster = new SimpleTextPaster(8, 8, textColorGenerator, true);
              //backgroundGenerator = new MultipleShapeBackgroundGenerator(DEFAULT_BACKGROUND_WIDTH, DEFAULT_BACKGROUND_HEIGHT);
                backgroundGenerator = new GradientBackgroundGenerator(DEFAULT_BACKGROUND_WIDTH, DEFAULT_BACKGROUND_HEIGHT, colorGray,colorWhite);
                break;
            
            	
        }

        RandomFontGenerator randomFontGenerator = new RandomFontGenerator(DEFAULT_MIN_FONT_SIZE, DEFAULT_MAX_FONT_SIZE,
                fontList.toArray(new Font[fontList.size()]));

        ComposedWordToImage composedWordToImage = new ComposedWordToImage(randomFontGenerator, backgroundGenerator,
                simpleWhitePaster);

        GimpyFactory gimpyFactory = new GimpyFactory(new RandomWordsGenerator(charSet), composedWordToImage);

        GenericCaptchaEngine genericCaptchaEngine = new GenericCaptchaEngine(new CaptchaFactory[] { gimpyFactory });

        genericManageableCaptchaService = new GenericManageableCaptchaService(genericCaptchaEngine, 180, 180000);

    }

}
