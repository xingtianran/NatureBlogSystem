package net.nature.blog.utils;

public interface Constants {
    String DEFAULT_STATE = "1";
    String DISABLE_STATE = "0";

    String YES_CACHE = "1";
    String No_CACHE = "0";

    String YES_ADMIN = "1";
    String NO_ADMIN = "0";
    interface User{
        String ROLE_ADMIN = "role_admin";
        String ROLE_NORMAL = "role_normal";
        String DEFAULT_AVATAR = "1701098204088_1178836838752714752.png";
        String DEFAULT_STATE = "1";
        String COOKIE_TOKEN_KEY = "nature_blog_token";

        //redis_key
        String KEY_CAPTCHA_CONTENT = "key_captcha_content_";
        String KEY_EMAIL_CONTENT = "key_email_content";
        String KEY_EMAIL_SEND_ADDRESS = "key_email_send_address";
        String KEY_EMAIL_SEND_IP = "key_email_send_ip";
        String KEY_TOKEN = "key_token_";
        String KEY_COMMIT_SIGN = "key_commit_sign_";

        String FROM_PC = "p_";
        String FROM_MOBILE = "m_";
    }

    interface Setting{
        String MANAGER_ACCOUNT_STATE = "manager_account_state";
        String KEY_WEBSITE_TITLE = "key_website_title";
        String KEY_WEBSITE_DESCRIPTION = "key_website_description";
        String KEY_WEBSITE_KEYWORDS = "key_website_keywords";
        String KEY_WEBSITE_VIEW_COUNT = "key_website_view_count";
    }

    interface Page{
        int DEFAULT_PAGE = 1;
        int DEFAULT_SIZE = 5;
    }

    interface TimeValueInMillions{
        long MIN = 60 * 1000;
        long HOUR = MIN * 60;
        long DAY = HOUR * 24;
        long WEEK = DAY * 7;
        long MONTH = DAY * 30;
        long YEAR = MONTH * 12;
    }
    interface TimeValueInSecond{
        int SECOND = 1;
        int MIN = 60;
        int HOUR = MIN * 60;
        int DAY = HOUR * 24;
        int WEEK = DAY * 7;
        int MONTH = DAY * 30;
        int YEAR = MONTH * 12;
    }

    interface ImageType{
        String PREFIX = "image/";
        String TYPE_JPG = "jpg";
        String TYPE_PNG = "png";
        String TYPE_GIF = "gif";
        String TYPE_JPG_WITH_PREFIX = PREFIX + "jpg";
        String TYPE_JPEG_WITH_PREFIX = PREFIX + "jpeg";
        String TYPE_PNG_WITH_PREFIX = PREFIX + "png";
        String TYPE_GIF_WITH_PREFIX = PREFIX + "gif";
    }

    interface Article{
        int TITLE_MAX_LENGTH = 128;
        int SUMMARY_MAX_LENGTH = 256;
        String TYPE_RICH_TEXT = "0";
        String TYPE_MARKDOWN = "1";
        // 0表示删除 1表示已经发布 2表示草稿 3表示置顶
        String STATE_DELETE = "0";
        String STATE_PUBLISH = "1";
        String STATE_DRAFT = "2";
        String NO_TOP = "0";
        String YES_TOP = "1";
        String TIME_ASC = "1";
        String TIME_DESC = "2";
        String VIEW_COUNT_ASC = "3";
        String VIEW_COUNT_DESC = "4";
        String KEY_ARTICLE_CACHE = "key_article_cache_";
        String KEY_VIEW_COUNT_CACHE = "key_view_count_cache_";
        String KEY_ARTICLE_FIRST_PAGE_CACHE = "key_article_first_page_cache";
    }

    interface Comment{
        String NO_TOP = "0";
        String YES_TOP = "1";

        String KEY_COMMENT_FIRST_PAGE_CACHE = "key_comment_first_page_cache_";
    }

}
