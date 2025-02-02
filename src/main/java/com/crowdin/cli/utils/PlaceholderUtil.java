package com.crowdin.cli.utils;

import com.crowdin.cli.client.LanguageMapping;
import com.crowdin.client.languages.model.Language;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PlaceholderUtil {

    public static final String PLACEHOLDER_ANDROID_CODE = "%android_code%";
    public static final String PLACEHOLDER_LANGUAGE = "%language%";
    public static final String PLACEHOLDER_LOCALE = "%locale%";
    public static final String PLACEHOLDER_LOCALE_WITH_UNDERSCORE = "%locale_with_underscore%";
    public static final String PLACEHOLDER_THREE_LETTERS_CODE = "%three_letters_code%";
    public static final String PLACEHOLDER_TWO_LETTERS_CODE = "%two_letters_code%";
    public static final String PLACEHOLDER_OSX_CODE = "%osx_code%";
    public static final String PLACEHOLDER_OSX_LOCALE = "%osx_locale%";
    public static final String PLACEHOLDER_LANGUAGE_ID = "%language_id%";

    public static final String PLACEHOLDER_FILE_EXTENTION = "%file_extension%";
    public static final String PLACEHOLDER_FILE_NAME = "%file_name%";
    public static final String PLACEHOLDER_ORIGINAL_FILE_NAME = "%original_file_name%";
    public static final String PLACEHOLDER_ORIGINAL_PATH = "%original_path%";

    private static final String DOUBLED_ASTERISK = "**";
    private static final String REGEX = "regex";
    private static final String ASTERISK = "*";
    private static final String QUESTION_MARK = "?";
    private static final String DOT = ".";
    private static final String DOT_PLUS = ".+";
    private static final String SET_OPEN_BRECKET = "[";
    private static final String SET_CLOSE_BRECKET = "]";
    private static final String ROUND_BRACKET_OPEN = "(";
    private static final String ROUND_BRACKET_CLOSE = ")";
    private static final String ESCAPE_ROUND_BRACKET_OPEN = "\\(";
    private static final String ESCAPE_ROUND_BRACKET_CLOSE = "\\)";
    private static final String ESCAPE_DOT = "\\.";
    private static final String ESCAPE_DOT_PLACEHOLDER = "{ESCAPE_DOT}";
    private static final String ESCAPE_QUESTION = "\\?";
    private static final String ESCAPE_QUESTION_PLACEHOLDER = "{ESCAPE_QUESTION_MARK}";
    private static final String ESCAPE_ASTERISK = "\\*";
    private static final String ESCAPE_ASTERISK_PLACEHOLDER = "{ESCAPE_ASTERISK}";

    private List<Language> supportedLangs;
    private List<Language> projectLangs;
    private String basePath;

    public PlaceholderUtil(List<Language> supportedLangs, List<com.crowdin.client.languages.model.Language> projectLangs, String basePath) {
        if (supportedLangs == null || projectLangs == null || basePath == null) {
            throw new NullPointerException("in PlaceholderUtil.contructor");
        }
        this.supportedLangs = supportedLangs;
        this.projectLangs = projectLangs;
        this.basePath = basePath;
    }

    public List<String> format(List<File> sources, List<String> toFormat, boolean onProjectLangs) {
        if (sources == null || toFormat == null) {
            return new ArrayList<>();
        }
        List<String> res = new ArrayList<>();
        for (String str : toFormat) {
            res.addAll(this.format(sources, str, onProjectLangs));
        }
        return res;
    }

    public Set<String> format(List<File> sources, String toFormat, boolean onProjectLangs) {
        if (sources == null || toFormat == null) {
            return new HashSet<>();
        }
        List<Language> languages = (onProjectLangs ? projectLangs : supportedLangs);
        Set<String> result = languages.stream()
                .map(lang -> this.replaceLanguageDependentPlaceholders(toFormat, lang))
                .flatMap(changedToFormat -> sources.stream()
                        .map(source -> this.replaceFileDependentPlaceholders(changedToFormat, source)))
                .collect(Collectors.toSet());
        return result;
    }

    public String replaceLanguageDependentPlaceholders(String toFormat, Language lang) {
        if (toFormat == null || lang == null) {
            throw new NullPointerException("null args in replaceLanguageDependentPlaceholders()");
        }
        return toFormat
                .replace(PLACEHOLDER_LANGUAGE_ID, lang.getId())
                .replace(PLACEHOLDER_LANGUAGE, lang.getName())
                .replace(PLACEHOLDER_LOCALE, lang.getLocale())
                .replace(PLACEHOLDER_LOCALE_WITH_UNDERSCORE, lang.getLocale().replace("-", "_"))
                .replace(PLACEHOLDER_TWO_LETTERS_CODE, lang.getTwoLettersCode())
                .replace(PLACEHOLDER_THREE_LETTERS_CODE, lang.getThreeLettersCode())
                .replace(PLACEHOLDER_ANDROID_CODE, lang.getAndroidCode())
                .replace(PLACEHOLDER_OSX_LOCALE, lang.getOsxLocale())
                .replace(PLACEHOLDER_OSX_CODE, lang.getOsxCode());
    }

    public List<String> replaceLanguageDependentPlaceholders(String toFormat, LanguageMapping languageMapping) {
        return projectLangs
            .stream()
            .map(lang -> replaceLanguageDependentPlaceholders(toFormat, languageMapping, lang))
            .collect(Collectors.toList());
    }

    public String replaceLanguageDependentPlaceholders(String toFormat, LanguageMapping langMapping, Language lang) {
        if (toFormat == null || lang == null || langMapping == null) {
            throw new NullPointerException("null args in replaceLanguageDependentPlaceholders()");
        }
        return toFormat
            .replaceAll(PLACEHOLDER_LANGUAGE_ID, langMapping.getValueOrDefault(lang.getId(),
                PLACEHOLDER_LANGUAGE_ID.replaceAll("%", ""), lang.getId()))
            .replaceAll(PLACEHOLDER_LANGUAGE, langMapping.getValueOrDefault(lang.getId(),
                PLACEHOLDER_LANGUAGE.replaceAll("%", ""), langMapping.getValueOrDefault(
                    lang.getId(), "name", lang.getName())))
            .replaceAll(PLACEHOLDER_LOCALE, langMapping.getValueOrDefault(lang.getId(),
                PLACEHOLDER_LOCALE.replaceAll("%", ""), lang.getLocale()))
            .replaceAll(PLACEHOLDER_LOCALE_WITH_UNDERSCORE, langMapping.getValueOrDefault(lang.getId(),
                PLACEHOLDER_LOCALE_WITH_UNDERSCORE.replaceAll("%", ""), lang.getLocale().replace("-", "_")))
            .replaceAll(PLACEHOLDER_TWO_LETTERS_CODE, langMapping.getValueOrDefault(lang.getId(),
                PLACEHOLDER_TWO_LETTERS_CODE.replaceAll("%", ""), lang.getTwoLettersCode()))
            .replaceAll(PLACEHOLDER_THREE_LETTERS_CODE, langMapping.getValueOrDefault(lang.getId(),
                PLACEHOLDER_THREE_LETTERS_CODE.replaceAll("%", ""), lang.getThreeLettersCode()))
            .replaceAll(PLACEHOLDER_ANDROID_CODE, langMapping.getValueOrDefault(lang.getId(),
                PLACEHOLDER_ANDROID_CODE.replaceAll("%", ""), lang.getAndroidCode()))
            .replaceAll(PLACEHOLDER_OSX_LOCALE, langMapping.getValueOrDefault(lang.getId(),
                PLACEHOLDER_OSX_LOCALE.replaceAll("%", ""), lang.getOsxLocale()))
            .replaceAll(PLACEHOLDER_OSX_CODE, langMapping.getValueOrDefault(lang.getId(),
                PLACEHOLDER_OSX_CODE.replaceAll("%", ""), lang.getOsxCode()));
    }

    public String replaceFileDependentPlaceholders(String toFormat, File file) {
        if (toFormat == null || file == null) {
            throw new NullPointerException("null args in replaceFileDependentPlaceholders()");
        }
        String fileName = file.getName();
        String fileNameWithoutExt = FilenameUtils.removeExtension(fileName);
        String fileExt = FilenameUtils.getExtension(fileName);
        String tempBasePath = basePath;
        String fileParent = StringUtils.removeStart((file.getParent() != null ? file.getParent() + Utils.PATH_SEPARATOR : ""), tempBasePath);
        toFormat = toFormat
                .replace(PLACEHOLDER_ORIGINAL_FILE_NAME, fileName)
                .replace(PLACEHOLDER_FILE_NAME, fileNameWithoutExt)
                .replace(PLACEHOLDER_FILE_EXTENTION, fileExt)
                .replace(PLACEHOLDER_ORIGINAL_PATH, fileParent);
        String doubleAsterisks =
            Utils.PATH_SEPARATOR
                + StringUtils.removeStart(fileParent,
                    StringUtils.removeStart(StringUtils.substringBefore(toFormat, Utils.PATH_SEPARATOR + "**"), Utils.PATH_SEPARATOR));
        toFormat = toFormat
                .replace(Utils.PATH_SEPARATOR + "**", doubleAsterisks)
                .replaceAll("[\\\\/]+", Utils.PATH_SEPARATOR_REGEX);
        return StringUtils.removeStart(toFormat, Utils.PATH_SEPARATOR);
    }

    public List<String> formatForRegex(List<String> toFormat, boolean onProjectLangs) {
        List<Language> langs = (onProjectLangs) ? this.projectLangs : this.supportedLangs;
        String langIds = langs.stream().map(Language::getId).collect(Collectors.joining("|", "(", ")"));
        String langNames = langs.stream().map(Language::getName).collect(Collectors.joining("|", "(", ")"));
        String langLocales = langs.stream().map(Language::getLocale).collect(Collectors.joining("|", "(", ")"));
        String langLocalesWithUnderscore = langs.stream().map(Language::getLocale).map(s -> s.replace("-", "_"))
            .collect(Collectors.joining("|", "(", ")"));
        String langTwoLettersCodes = langs.stream().map(Language::getTwoLettersCode).collect(Collectors.joining("|", "(", ")"));
        String langThreeLettersCodes = langs.stream().map(Language::getThreeLettersCode).collect(Collectors.joining("|", "(", ")"));
        String langAndroidCodes = langs.stream().map(Language::getAndroidCode).collect(Collectors.joining("|", "(", ")"));
        String langOsxLocales = langs.stream().map(Language::getOsxLocale).collect(Collectors.joining("|", "(", ")"));
        String langOsxCodes = langs.stream().map(Language::getOsxCode).collect(Collectors.joining("|", "(", ")"));
        return toFormat.stream()
            .map(s -> s
                .replace(ESCAPE_DOT, ESCAPE_DOT_PLACEHOLDER)
                .replace(DOT, ESCAPE_DOT)
                .replace(ESCAPE_DOT_PLACEHOLDER, ESCAPE_DOT)

                .replace(ESCAPE_QUESTION, ESCAPE_QUESTION_PLACEHOLDER)
                .replace(QUESTION_MARK, "[^/]")
                .replace(ESCAPE_QUESTION_PLACEHOLDER, ESCAPE_QUESTION)

                .replace(ESCAPE_ASTERISK, ESCAPE_ASTERISK_PLACEHOLDER)
                .replace("**", ".+")
                .replace(ESCAPE_ASTERISK_PLACEHOLDER, ESCAPE_ASTERISK)

                .replace(ESCAPE_ASTERISK, ESCAPE_ASTERISK_PLACEHOLDER)
                .replace(ASTERISK, "[^/]+")
                .replace(ESCAPE_ASTERISK_PLACEHOLDER, ESCAPE_ASTERISK)

                .replace(ROUND_BRACKET_OPEN, ESCAPE_ROUND_BRACKET_OPEN)

                .replace(ROUND_BRACKET_CLOSE, ESCAPE_ROUND_BRACKET_CLOSE))
            .map(s -> s
                .replace(PLACEHOLDER_FILE_EXTENTION, "[^/]+")
                .replace(PLACEHOLDER_FILE_NAME, "[^/]+")
                .replace(PLACEHOLDER_ORIGINAL_FILE_NAME, "[^/]+")
                .replace(PLACEHOLDER_ORIGINAL_PATH, ".+"))
            .map(s -> s
                .replace(PLACEHOLDER_LANGUAGE_ID, langIds)
                .replace(PLACEHOLDER_LANGUAGE, langNames)
                .replace(PLACEHOLDER_LOCALE, langLocales)
                .replace(PLACEHOLDER_LOCALE_WITH_UNDERSCORE, langLocalesWithUnderscore)
                .replace(PLACEHOLDER_TWO_LETTERS_CODE, langTwoLettersCodes)
                .replace(PLACEHOLDER_THREE_LETTERS_CODE, langThreeLettersCodes)
                .replace(PLACEHOLDER_ANDROID_CODE, langAndroidCodes)
                .replace(PLACEHOLDER_OSX_LOCALE, langOsxLocales)
                .replace(PLACEHOLDER_OSX_CODE, langOsxCodes))
            .map(s -> "^" + s + "$")
            .collect(Collectors.toList());
    }

    public static boolean containsFilePlaceholders(String pattern) {
        return StringUtils.containsAny(pattern,
            PLACEHOLDER_FILE_EXTENTION,
            PLACEHOLDER_FILE_NAME,
            PLACEHOLDER_ORIGINAL_FILE_NAME,
            PLACEHOLDER_ORIGINAL_PATH);
    }

    public static boolean containsLangPlaceholders(String translationsPattern) {
        return StringUtils.containsAny(translationsPattern,
            PLACEHOLDER_LANGUAGE,
            PLACEHOLDER_TWO_LETTERS_CODE,
            PLACEHOLDER_THREE_LETTERS_CODE,
            PLACEHOLDER_LOCALE_WITH_UNDERSCORE,
            PLACEHOLDER_LOCALE,
            PLACEHOLDER_ANDROID_CODE,
            PLACEHOLDER_OSX_CODE,
            PLACEHOLDER_OSX_LOCALE);
    }
}
