package com.kelompok1.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.title.TextTitle;

import javax.swing.*;
import java.awt.*;

public class ChartUtils {

    /**
     * Creates a styled Bar Chart Panel for "Top Books".
     */
    public static ChartPanel createTopBooksBarChart(DefaultCategoryDataset dataset, String title) {
        JFreeChart chart = ChartFactory.createBarChart(
                "", // Title handled via UI components mostly
                "Buku",
                "Jumlah Peminjaman",
                dataset,
                PlotOrientation.HORIZONTAL,
                false, // legend
                true,  // tooltips
                false  // urls
        );

        applyModernTheme(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        // Modern colors
        renderer.setSeriesPaint(0, DesignSystem.PRIMARY);
        renderer.setShadowVisible(false);
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        renderer.setMaximumBarWidth(0.5); // Thinner bars for elegance

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chartPanel.setBackground(DesignSystem.SURFACE);
        chartPanel.setOpaque(false);
        return chartPanel;
    }

    /**
     * Creates a styled Line Chart Panel for "Circulation Trends".
     */
    public static ChartPanel createCirculationLineChart(DefaultCategoryDataset dataset, String title) {
        JFreeChart chart = ChartFactory.createLineChart(
                "",
                "Tanggal",
                "Jumlah",
                dataset,
                PlotOrientation.VERTICAL,
                true,  // legend
                true,  // tooltips
                false  // urls
        );

        applyModernTheme(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        
        // Peminjaman (Borrowed)
        renderer.setSeriesPaint(0, DesignSystem.PRIMARY);
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesShapesVisible(0, true);
        
        // Pengembalian (Returned)
        renderer.setSeriesPaint(1, DesignSystem.TERTIARY);
        renderer.setSeriesStroke(1, new BasicStroke(2.5f));
        renderer.setSeriesShapesVisible(1, true);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chartPanel.setBackground(DesignSystem.SURFACE);
        chartPanel.setOpaque(false);
        return chartPanel;
    }

    private static void applyModernTheme(JFreeChart chart) {
        chart.setBackgroundPaint(DesignSystem.SURFACE);
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(DesignSystem.SURFACE);
        plot.setOutlineVisible(false);
        
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(DesignSystem.OUTLINE_VARIANT);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(DesignSystem.OUTLINE_VARIANT);

        // Customize axes
        Font axisFont = DesignSystem.bodyFont(11f, Font.PLAIN);
        Color axisColor = DesignSystem.ON_SURFACE_VARIANT;

        plot.getDomainAxis().setTickLabelFont(axisFont);
        plot.getDomainAxis().setTickLabelPaint(axisColor);
        plot.getDomainAxis().setLabelFont(axisFont);
        plot.getDomainAxis().setLabelPaint(axisColor);
        plot.getDomainAxis().setAxisLinePaint(DesignSystem.OUTLINE_VARIANT);

        plot.getRangeAxis().setTickLabelFont(axisFont);
        plot.getRangeAxis().setTickLabelPaint(axisColor);
        plot.getRangeAxis().setLabelFont(axisFont);
        plot.getRangeAxis().setLabelPaint(axisColor);
        plot.getRangeAxis().setAxisLinePaint(DesignSystem.OUTLINE_VARIANT);

        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(DesignSystem.SURFACE);
            chart.getLegend().setItemFont(axisFont);
            chart.getLegend().setItemPaint(DesignSystem.ON_SURFACE);
            chart.getLegend().setBorder(0, 0, 0, 0);
        }
    }
}
