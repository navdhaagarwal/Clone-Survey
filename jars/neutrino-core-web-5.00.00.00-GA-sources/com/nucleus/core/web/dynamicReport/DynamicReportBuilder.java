/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.web.dynamicReport;

import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.constant.ImageType;
import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.builder.column.PercentageColumnBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.datatype.BigDecimalType;
import net.sf.dynamicreports.report.builder.datatype.BooleanType;
import net.sf.dynamicreports.report.builder.datatype.DataTypes;
import net.sf.dynamicreports.report.builder.datatype.IntegerType;
import net.sf.dynamicreports.report.builder.group.Groups;
import net.sf.dynamicreports.report.builder.style.FontBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.builder.subtotal.SubtotalBuilders;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.LineStyle;
import net.sf.dynamicreports.report.constant.Position;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.definition.expression.DRIValueFormatter;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.data.JRMapArrayDataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.springframework.core.io.ClassPathResource;

import com.nucleus.core.dynamicQuery.entity.QueryTokenValue;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.money.entity.Money;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.UserService;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
@Named("dynamicReportBuilder")
public class DynamicReportBuilder {

    private static final Map<String, String[]> reportExportTypes    = new HashMap<String, String[]>();

    private static final Map<Integer, String>  chartTypes           = new HashMap<Integer, String>();

    private static CustomBooleanType           CUSTOM_BOOLEAN_TYPE  = new CustomBooleanType();

    private static final String                SR_NUMBER_COL_TITLE  = "Sr.No.";

    static {

        reportExportTypes.put("PDF", new String[] { "application/octet-stream", "pdf" });
        reportExportTypes.put("HTML", new String[] { "text/html", "html" });
        reportExportTypes.put("CSV", new String[] { "text/plain", "txt" });
        reportExportTypes.put("DOCX", new String[] { "application/octet-stream", "docx" });
        reportExportTypes.put("JPG", new String[] { "image/jpeg", "jpg" });

        chartTypes.put(100, "Bar Chart");
        chartTypes.put(101, "Pie Chart");
        chartTypes.put(102, "Bar3D Chart");
        chartTypes.put(103, "Pie3D Chart");
    }

    FontBuilder                                DEFAULT_FONT         = Styles.font(Font.SANS_SERIF, false, false, 5);

    StyleBuilder                               boldStyle            = stl.style().bold();

    StyleBuilder                               boldFont10Style      = stl.style(boldStyle).setFontSize(10);

    StyleBuilder                               groupNameStyle       = stl.style().bold().setFontSize(12).setTopPadding(10)
                                                                            .setForegroundColor(new Color(0, 30, 174));
    StyleBuilder                               boldCenteredStyle    = stl.style(boldStyle).setHorizontalAlignment(
                                                                            HorizontalAlignment.CENTER);
    StyleBuilder                               columnTitleStyle     = stl.style(boldCenteredStyle)
                                                                            .setBorder(stl.pen1Point())
                                                                            .setBackgroundColor(new Color(255, 149, 96));

    StyleBuilder                               groupTotalStyle      = stl.style(boldStyle).setSpacingBefore(2)
                                                                            .setBackgroundColor(new Color(255, 201, 174))
                                                                            .setForegroundColor(new Color(0, 30, 174));

    StyleBuilder                               groupTotalLabelStyle = stl.style(boldStyle).setLeftPadding(5)
                                                                            .setBackgroundColor(new Color(255, 201, 174))
                                                                            .setForegroundColor(new Color(0, 30, 174));

    StyleBuilder                               reportTitleStyle     = stl.style(boldCenteredStyle).setFontSize(20);

    StyleBuilder                               summeryTotalStyle    = stl.style(boldStyle).setLeftPadding(5)
                                                                            .setTopBorder(Styles.pen(1f, LineStyle.SOLID));

    @Inject
    @Named("userService")
    protected UserService                      userService;

    public DynamicReportPojo generateReport(DynamicReportConfig dynamicReportConfig) {

        List<Map<String, Object>> dataList = dynamicReportConfig.getDataList();

        // sort only if grouping is required
        if (StringUtils.isNotBlank(dynamicReportConfig.getGroupByTokenName())) {
            sortList(dataList, dynamicReportConfig.getGroupByTokenName());
        }

        try {
            byte[] reportData = doGenerate(dataList, dynamicReportConfig);
            String fileName = dynamicReportConfig.getReportTitle().concat(".")
                    .concat(reportExportTypes.get(dynamicReportConfig.getExportType())[1]);
            DynamicReportPojo reportPojo = new DynamicReportPojo(reportData, reportExportTypes.get(dynamicReportConfig
                    .getExportType())[0], fileName);

            return reportPojo;
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Error in generating dynamic report", e);
            throw new SystemException("Error in generating dynamic report", e);
        }

    }

    private byte[] doGenerate(List<Map<String, Object>> dataList, DynamicReportConfig config) throws DRException,
            FileNotFoundException {

        JasperReportBuilder report = DynamicReports.report();

        if (dataList != null && !dataList.isEmpty()) {
            Map<String, Object> dataMap = new HashMap<String, Object>(dataList.get(0));
            Set<String> keys = new HashSet<String>();
            for (Entry<String, Object> entry : dataMap.entrySet()) {
                if (entry.getValue() == null) {
                    keys.add(entry.getKey());
                }
            }
            // if value in first data map is null.It may be not null for later data records.
            // So search for first not null value if exists
            for (String nullValueKey : keys) {
                for (Map<String, Object> map : dataList) {
                    if (map.get(nullValueKey) != null) {
                        dataMap.put(nullValueKey, map.get(nullValueKey));
                        break;
                    }
                }
            }

            // first of all add a Sr. No. column
            TextColumnBuilder<Integer> rowNumberColumn = col.reportRowNumberColumn(SR_NUMBER_COL_TITLE)
                    .setHorizontalAlignment(HorizontalAlignment.LEFT);
            report.addColumn(rowNumberColumn);

            for (String key : dataMap.keySet()) {

                Object object = dataMap.get(key);

                if (object instanceof String) {
                    TextColumnBuilder<String> stringColum = Columns.column(key, key, DataTypes.stringType());

                    // if it is a group column,no need to add it by
                    // report.addColumn
                    if (config.getGroupByTokenName() != null && config.getGroupByTokenName().equals(key)) {
                        report.addGroup(Groups.group(stringColum).setStyle(groupNameStyle));
                        continue;
                    }
                    report.addColumn(stringColum);

                    if (config.getCountForTokenNames().contains(key)) {

                        if (config.isCountAtGroup()) {
                            report.addSubtotalAtLastGroupFooter(new SubtotalBuilders().count(stringColum)
                                    .setStyle(groupTotalStyle).setLabelPosition(Position.LEFT)
                                    .setLabelStyle(groupTotalLabelStyle).setLabel("Count"));
                        }
                        if (config.isCountAtSummary()) {
                            report.addSubtotalAtSummary(new SubtotalBuilders().count(stringColum)
                                    .setStyle(summeryTotalStyle).setLabelPosition(Position.LEFT)
                                    .setLabelStyle(summeryTotalStyle).setLabel("Count"));
                        }

                    }

                } else if (object instanceof Character) {

                    TextColumnBuilder<Character> charColumn = Columns.column(key, key, DataTypes.characterType());
                    if (config.getGroupByTokenName() != null && config.getGroupByTokenName().equals(key)) {
                        report.addGroup(Groups.group(charColumn).setStyle(groupNameStyle));
                        continue;
                    }

                    report.addColumn(charColumn);

                    if (config.getCountForTokenNames().contains(key)) {

                        if (config.isCountAtGroup()) {
                            report.addSubtotalAtLastGroupFooter(new SubtotalBuilders().count(charColumn)
                                    .setStyle(groupTotalStyle).setLabelPosition(Position.LEFT)
                                    .setLabelStyle(groupTotalLabelStyle).setLabel("Count"));
                        }
                        if (config.isCountAtSummary()) {
                            report.addSubtotalAtSummary(new SubtotalBuilders().count(charColumn).setStyle(summeryTotalStyle)
                                    .setLabelPosition(Position.LEFT).setLabelStyle(summeryTotalStyle).setLabel("Count"));
                        }

                    }

                } else if (object instanceof Boolean) {

                    TextColumnBuilder<Boolean> boolColumn = Columns.column(key, key, CUSTOM_BOOLEAN_TYPE);

                    if (config.getGroupByTokenName() != null && config.getGroupByTokenName().equals(key)) {
                        report.addGroup(Groups.group(boolColumn).setStyle(groupNameStyle));
                        continue;
                    }
                    report.addColumn(boolColumn);

                    if (config.getCountForTokenNames().contains(key)) {

                        if (config.isCountAtGroup()) {
                            report.addSubtotalAtLastGroupFooter(new SubtotalBuilders().count(boolColumn)
                                    .setStyle(groupTotalStyle).setLabelPosition(Position.LEFT)
                                    .setLabelStyle(groupTotalLabelStyle).setLabel("Count"));
                        }
                        if (config.isCountAtSummary()) {
                            report.addSubtotalAtSummary(new SubtotalBuilders().count(boolColumn).setStyle(summeryTotalStyle)
                                    .setLabelPosition(Position.LEFT).setLabelStyle(summeryTotalStyle).setLabel("Count"));
                        }

                    }

                }

                else if (object instanceof Byte) {

                    TextColumnBuilder<Byte> byteColumn = Columns.column(key, key, DataTypes.byteType());

                    if (config.getGroupByTokenName() != null && config.getGroupByTokenName().equals(key)) {
                        report.addGroup(Groups.group(byteColumn).setStyle(groupNameStyle));
                        continue;
                    }
                    report.addColumn(byteColumn);

                    addSubtotals(byteColumn, report, config, key);

                } else if (object instanceof Short) {

                    TextColumnBuilder<Short> shortColumn = Columns.column(key, key, DataTypes.shortType());

                    if (config.getGroupByTokenName() != null && config.getGroupByTokenName() != null
                            && config.getGroupByTokenName().equals(key)) {
                        report.addGroup(Groups.group(shortColumn).setStyle(groupNameStyle));
                        continue;
                    }
                    report.addColumn(shortColumn);

                    addSubtotals(shortColumn, report, config, key);
                }

                else if (object instanceof Integer) {

                    IntegerType finalType = DataTypes.integerType();
                    // special handling of integer constants like 0 for INDIVIDUAL etc.
                    List<QueryTokenValue> tokenValues = config.getTokenValuesIfAnyForSelectedQueryToken(key);
                    if (!tokenValues.isEmpty()) {
                        Map<String, String> valueStringMap = new HashMap<String, String>();
                        for (QueryTokenValue queryTokenValue : tokenValues) {
                            valueStringMap.put(queryTokenValue.getActualValue(), queryTokenValue.getDisplayName());
                        }
                        finalType = new CustomIntegerType(valueStringMap);
                    }

                    TextColumnBuilder<Integer> integerColumn = Columns.column(key, key, finalType);
                    if (config.getGroupByTokenName() != null && config.getGroupByTokenName().equals(key)) {
                        report.addGroup(Groups.group(integerColumn).setStyle(groupNameStyle));
                        continue;
                    }

                    report.addColumn(integerColumn);

                    addSubtotals(integerColumn, report, config, key);
                } else if (object instanceof Long) {

                    TextColumnBuilder<Long> longColumn = Columns.column(key, key, DataTypes.longType());
                    if (config.getGroupByTokenName() != null && config.getGroupByTokenName().equals(key)) {
                        report.addGroup(Groups.group(longColumn).setStyle(groupNameStyle));
                        continue;
                    }

                    report.addColumn(longColumn);

                    addSubtotals(longColumn, report, config, key);
                } else if (object instanceof Float) {

                    TextColumnBuilder<Float> floatColumn = Columns.column(key, key, DataTypes.floatType());
                    if (config.getGroupByTokenName() != null && config.getGroupByTokenName().equals(key)) {
                        report.addGroup(Groups.group(floatColumn).setStyle(groupNameStyle));
                        continue;
                    }

                    report.addColumn(floatColumn);

                    addSubtotals(floatColumn, report, config, key);
                } else if (object instanceof Double) {

                    TextColumnBuilder<Double> doubleColumn = Columns.column(key, key, DataTypes.doubleType());
                    if (config.getGroupByTokenName() != null && config.getGroupByTokenName().equals(key)) {
                        report.addGroup(Groups.group(doubleColumn).setStyle(groupNameStyle));
                        continue;
                    }

                    report.addColumn(doubleColumn);
                    addSubtotals(doubleColumn, report, config, key);
                } else if (object instanceof Number) {

                    TextColumnBuilder<? extends Number> numberColumn = null;

                    if (object instanceof BigDecimal) {
                        numberColumn = Columns.column(key, key,
                                config.isCurrencyType(key) ? new CurrencyType() : DataTypes.bigDecimalType());
                    } else if (object instanceof BigInteger) {
                        numberColumn = Columns.column(key, key, DataTypes.bigIntegerType());
                    }

                    if (config.getGroupByTokenName() != null && config.getGroupByTokenName().equals(key)) {
                        report.addGroup(Groups.group(numberColumn).setStyle(groupNameStyle));
                        continue;
                    }

                    report.addColumn(numberColumn);

                    addSubtotals(numberColumn, report, config, key);

                } else if (object instanceof DateTime) {

                    TextColumnBuilder<DateTime> dateTimeBuilder = Columns.column(key, key,
                            new DateTimeType(userService.getUserPreferredDateFormat()));
                    if (config.getGroupByTokenName() != null && config.getGroupByTokenName().equals(key)) {
                        report.addGroup(Groups.group(dateTimeBuilder).setStyle(groupNameStyle));
                        continue;
                    }
                    report.addColumn(dateTimeBuilder);
                } else if (object instanceof LocalDate) {

                    TextColumnBuilder<LocalDate> dateTimeBuilder = Columns.column(key, key,
                            new LocalDateType(userService.getUserPreferredDateFormat()));
                    if (config.getGroupByTokenName() != null && config.getGroupByTokenName().equals(key)) {
                        report.addGroup(Groups.group(dateTimeBuilder).setStyle(groupNameStyle));
                        continue;
                    }
                    report.addColumn(dateTimeBuilder);
                } else if (object instanceof Calendar) {
                    TextColumnBuilder<Calendar> dateTimeBuilder = Columns.column(key, key,
                            new CalendarType(userService.getUserPreferredDateFormat()));
                    if (config.getGroupByTokenName() != null && config.getGroupByTokenName().equals(key)) {
                        report.addGroup(Groups.group(dateTimeBuilder).setStyle(groupNameStyle));
                        continue;
                    }
                    report.addColumn(dateTimeBuilder);
                }

            }

        }

        ClassPathResource classPathResource = new ClassPathResource("dynamicReports/FinnoneNEO_logo.jpg");

        Image image = null;
        try {
            image = ImageIO.read(classPathResource.getInputStream());
        } catch (IOException e) {
            BaseLoggers.exceptionLogger.error("Error in loading dynamic report logo image from classpath.", e);
        }

        report.addTitle(Components
                .horizontalList()
                .add(Components.image(image).setFixedDimension(30, 30),
                        Components.text(config.getReportTitle()).setStyle(reportTitleStyle)
                                .setHorizontalAlignment(HorizontalAlignment.LEFT),
                        Components.text("Date : ".concat(DateTimeFormat.forStyle("LS").print(DateTime.now())))
                                .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                                .setStyle(stl.style().setFont(stl.font(Font.SANS_SERIF, true, true, 7)))).newRow()
                .add(Components.filler().setStyle(stl.style().setTopBorder(stl.pen2Point())).setFixedHeight(10)));

        report.setColumnTitleStyle(columnTitleStyle);
        report.setHighlightDetailEvenRows(true);
        // report.setPageColumnSpace(10);

        report.setColumnStyle(stl.style(2));

        report.addPageFooter(Components.pageXofY().setStyle(boldCenteredStyle));
        report.setDataSource(new JRMapArrayDataSource(dataList.toArray()));

        // if no results found give empty report with message
        report.addNoData(Components.horizontalList()
                .add(Components.text("No Results Found for the Query").setStyle(groupNameStyle)).newRow()
                .add(Components.text(config.getDynamicQueryWhereClause()).setStyle(boldFont10Style)));

        // report.setSummaryOnANewPage(true);
        report.setDefaultFont(DEFAULT_FONT);

        // Export report to desired format
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (config.getExportType().equalsIgnoreCase("PDF")) {
            report.toPdf(byteArrayOutputStream);
        } else if (config.getExportType().equalsIgnoreCase("HTML")) {
            report.toHtml(byteArrayOutputStream);
        } else if (config.getExportType().equalsIgnoreCase("CSV")) {
            report.toCsv(byteArrayOutputStream);
        } else if (config.getExportType().equalsIgnoreCase("DOCX")) {
            report.toDocx(byteArrayOutputStream);
        } else if (config.getExportType().equalsIgnoreCase("JPG")) {
            report.toImage(byteArrayOutputStream, ImageType.JPG);
        }
        byte[] result = byteArrayOutputStream.toByteArray();
        IOUtils.closeQuietly(byteArrayOutputStream);
        return result;

    }

    private void sortList(List<Map<String, Object>> maps, final String groupBy) {

        Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public int compare(Map<String, Object> m1, Map<String, Object> m2) {

                Object lhs = m1.get(groupBy);
                Object rhs = m2.get(groupBy);
                if (lhs instanceof Comparable && rhs instanceof Comparable) {
                    return ((Comparable) lhs).compareTo(rhs);
                }
                return 0;
            }
        };

        Collections.sort(maps, mapComparator);
    }

    private static class CurrencyType extends BigDecimalType {
        private static final long serialVersionUID = 1L;

        @Override
        public String getPattern() {
            return Money.getBaseCurrency().getCurrencyCode().concat(" #,##0.00");
        }
    }

    public List<String> getReportExportTypes() {
        return new ArrayList<String>(reportExportTypes.keySet());
    }

    public Map<Integer, String> getChartTypes() {

        return Collections.unmodifiableMap(chartTypes);
    }

    private <T extends Number> void addSubtotals(TextColumnBuilder<T> valueColumn, JasperReportBuilder report,
            DynamicReportConfig config, String key) {

        // count
        if (config.getCountForTokenNames().contains(key)) {

            if (config.isCountAtGroup()) {
                report.addSubtotalAtLastGroupFooter(new SubtotalBuilders().count(valueColumn).setStyle(groupTotalStyle)
                        .setLabelPosition(Position.LEFT).setLabelStyle(groupTotalLabelStyle).setLabel("Count"));
            }
            if (config.isCountAtSummary()) {
                report.addSubtotalAtSummary(new SubtotalBuilders().count(valueColumn).setStyle(summeryTotalStyle)
                        .setLabelPosition(Position.LEFT).setLabelStyle(summeryTotalStyle).setLabel("Count"));
            }

        }
        // Sum
        if (config.getSumForTokenNames().contains(key)) {

            if (config.isSumAtGroup()) {
                report.addSubtotalAtLastGroupFooter(new SubtotalBuilders().sum(valueColumn).setStyle(groupTotalStyle)
                        .setLabelPosition(Position.LEFT).setLabelStyle(groupTotalLabelStyle).setLabel("Sum"));
            }
            if (config.isSumAtSummary()) {
                report.addSubtotalAtSummary(new SubtotalBuilders().sum(valueColumn).setStyle(summeryTotalStyle)
                        .setLabelPosition(Position.LEFT).setLabelStyle(summeryTotalStyle).setLabel("Sum"));
            }
        }
        // Avg
        if (config.getAvgForTokenNames().contains(key)) {

            if (config.isAvgAtGroup()) {
                report.addSubtotalAtLastGroupFooter(new SubtotalBuilders().avg(valueColumn).setStyle(groupTotalStyle)
                        .setLabelPosition(Position.LEFT).setLabelStyle(groupTotalLabelStyle).setLabel("Avg."));
            }
            if (config.isAvgAtSummary()) {
                report.addSubtotalAtSummary(new SubtotalBuilders().avg(valueColumn).setStyle(summeryTotalStyle)
                        .setLabelPosition(Position.LEFT).setLabelStyle(summeryTotalStyle).setLabel("Avg."));
            }
        }
        // add Percentage column
        if (config.getPercentageForTokenNames().contains(key)) {
            PercentageColumnBuilder quantityPercentageColumn = col.percentageColumn(key.concat(" %"), valueColumn);
            report.addColumn(quantityPercentageColumn);
        }
    }

    private static class CustomBooleanType extends BooleanType {

        private static final long serialVersionUID = -4041623893227834343L;

        @Override
        public String getPattern() {
            return "YN";
        }

        @Override
        public String valueToString(Boolean value, Locale locale) {
            if (value != null) {
                return value ? "YES" : "NO";
            }
            return null;
        }

        @Override
        public DRIValueFormatter<String, Boolean> getValueFormatter() {
            return new BooleanTextValueFormatter();
        }

    }

    private static class CustomIntegerType extends IntegerType {

        Map<String, String> valueStringMap = new HashMap<String, String>();

        public CustomIntegerType(Map<String, String> valueStringMap) {
            this.valueStringMap = valueStringMap;
        }

        @Override
        public String getPattern() {
            return "";
        }

        @Override
        public String valueToString(Number value, Locale locale) {
            if (value != null) {
                return valueStringMap.get(value.toString());
            }
            return null;
        }

        @Override
        public DRIValueFormatter<String, Number> getValueFormatter() {
            return new NumberTextValueFormatter(valueStringMap);
        }

    }

    private static class BooleanTextValueFormatter extends AbstractValueFormatter<String, Boolean> {

        private static final long serialVersionUID = 3043341948080014003L;

        @Override
        public String format(Boolean value, ReportParameters reportParameters) {
            if (value != null) {
                return value ? "YES" : "NO";
            }
            return null;
        }

    }

    private static class NumberTextValueFormatter extends AbstractValueFormatter<String, Number> {

        private static final long serialVersionUID = 3043341948080014003L;

        Map<String, String>       valueStringMap   = new HashMap<String, String>();

        public NumberTextValueFormatter(Map<String, String> valueStringMap) {
            this.valueStringMap = valueStringMap;
        }

        @Override
        public String format(Number value, ReportParameters reportParameters) {
            if (value != null) {
                return valueStringMap.containsKey(value.toString()) ? valueStringMap.get(value.toString()) : value
                        .toString();
            }
            return null;
        }

    }

}
