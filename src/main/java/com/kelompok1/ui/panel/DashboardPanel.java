package com.kelompok1.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.model.DailyStats;
import com.kelompok1.model.Book;
import com.kelompok1.model.Transaction;
import com.kelompok1.service.BookService;
import com.kelompok1.service.FineService;
import com.kelompok1.service.TransactionService;
import com.kelompok1.service.UserService;
import com.kelompok1.util.DesignSystem;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {

    private final BookService bookService;
    private final TransactionService transactionService;
    private final UserService userService;
    private final FineService fineService;

    private JPanel kpiGrid;
    private JPanel chartCard;
    private javax.swing.table.DefaultTableModel overdueModel;
    private javax.swing.table.DefaultTableModel recentModel;
    private JPanel topBooksList;

    public DashboardPanel() {
        this.bookService = new BookService();
        this.transactionService = new TransactionService();
        this.userService = new UserService();
        this.fineService = new FineService();

        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));
        add(createDashboardContent(), BorderLayout.CENTER);
        loadData();
    }

    private JScrollPane createDashboardContent() {
        JPanel dashPanel = new ScrollablePanel();
        dashPanel.setLayout(new BoxLayout(dashPanel, BoxLayout.Y_AXIS));
        dashPanel.setBackground(UIManager.getColor("Panel.background"));
        dashPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // 1. KPI Grid
        kpiGrid = new JPanel(new GridLayout(2, 4, 15, 15));
        kpiGrid.setBackground(UIManager.getColor("Panel.background"));
        kpiGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        JLabel loadingLabel = new JLabel("Memuat statistik...");
        loadingLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +2");
        kpiGrid.add(loadingLabel);

        dashPanel.add(kpiGrid);
        dashPanel.add(Box.createVerticalStrut(20));

        // 2. Middle Section (Chart + Overdue Table)
        JPanel midSection = new JPanel(new GridBagLayout());
        midSection.setBackground(UIManager.getColor("Panel.background"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        
        // Chart Panel (Placeholder)
        chartCard = UIUtils.createCardPanel();
        chartCard.setLayout(new BorderLayout());
        JLabel lblChartTitle = new JLabel("Statistik Peminjaman");
        lblChartTitle.putClientProperty(FlatClientProperties.STYLE, "font: bold +2");
        lblChartTitle.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
        chartCard.add(lblChartTitle, BorderLayout.NORTH);
        
        JPanel chartPlaceholder = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawLine(20, getHeight()-20, getWidth()-20, getHeight()-20);
                g2.drawLine(20, 20, 20, getHeight()-20);
                
                g2.setColor(new Color(101, 183, 108));
                g2.setStroke(new BasicStroke(2f));
                g2.drawArc(40, 50, 100, 100, 0, 180);
                
                g2.setColor(new Color(230, 80, 80));
                g2.drawArc(80, 80, 100, 100, 0, 180);
            }
        };
        chartPlaceholder.setBackground(DesignSystem.SURFACE_CONTAINER_LOWEST);
        chartCard.add(chartPlaceholder, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.45; gbc.insets = new Insets(0, 0, 0, 10);
        midSection.add(chartCard, gbc);

        // Overdue Table Panel
        JPanel overdueCard = UIUtils.createCardPanel();
        overdueCard.setLayout(new BorderLayout());
        JLabel lblOverdueTitle = new JLabel("Riwayat Keterlambatan");
        lblOverdueTitle.putClientProperty(FlatClientProperties.STYLE, "font: bold +2");
        lblOverdueTitle.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        overdueCard.add(lblOverdueTitle, BorderLayout.NORTH);
        
        String[] colOverdue = {"ID Anggota", "Judul", "ISBN", "Tenggat Waktu", "Denda"};
        overdueModel = new javax.swing.table.DefaultTableModel(new Object[][]{}, colOverdue);
        JTable tblOverdue = UIUtils.createStyledTable(new Object[][]{}, colOverdue);
        tblOverdue.setModel(overdueModel);
        JScrollPane scrollOverdue = new JScrollPane(tblOverdue);
        scrollOverdue.setBorder(BorderFactory.createEmptyBorder());
        overdueCard.add(scrollOverdue, BorderLayout.CENTER);

        gbc.gridx = 1; gbc.weightx = 0.55; gbc.insets = new Insets(0, 10, 0, 0);
        midSection.add(overdueCard, gbc);

        midSection.setPreferredSize(new Dimension(getWidth(), 300));
        midSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));
        dashPanel.add(midSection);
        dashPanel.add(Box.createVerticalStrut(20));

        // 3. Bottom Section (Recent Checkouts + Top Books)
        JPanel botSection = new JPanel(new GridBagLayout());
        botSection.setBackground(UIManager.getColor("Panel.background"));
        GridBagConstraints gbcB = new GridBagConstraints();
        gbcB.fill = GridBagConstraints.BOTH;
        gbcB.weighty = 1.0;

        // Recent Checkouts
        JPanel recentCard = UIUtils.createCardPanel();
        recentCard.setLayout(new BorderLayout());
        JPanel recentHeader = new JPanel(new BorderLayout());
        recentHeader.setOpaque(false);
        recentHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        JLabel lblRecentTitle = new JLabel("Peminjaman Terbaru");
        lblRecentTitle.setFont(DesignSystem.displayFont(15f, Font.BOLD));
        lblRecentTitle.setForeground(DesignSystem.ON_SURFACE);
        JLabel lblViewAll = new JLabel("Lihat Semua");
        lblViewAll.setFont(DesignSystem.bodyFont(13f, Font.BOLD));
        lblViewAll.setForeground(DesignSystem.TERTIARY);
        recentHeader.add(lblRecentTitle, BorderLayout.WEST);
        recentHeader.add(lblViewAll, BorderLayout.EAST);
        recentCard.add(recentHeader, BorderLayout.NORTH);

        String[] colRecent = {"ID", "ID Buku", "Judul", "Anggota", "Tgl Pinjam", "Tgl Kembali"};
        recentModel = new javax.swing.table.DefaultTableModel(new Object[][]{}, colRecent);
        JTable tblRecent = UIUtils.createStyledTable(new Object[][]{}, colRecent);
        tblRecent.setModel(recentModel);
        JScrollPane scrollRecent = new JScrollPane(tblRecent);
        scrollRecent.setBorder(BorderFactory.createEmptyBorder());
        recentCard.add(scrollRecent, BorderLayout.CENTER);

        gbcB.gridx = 0; gbcB.gridy = 0; gbcB.weightx = 0.70; gbcB.insets = new Insets(0, 0, 0, 10);
        botSection.add(recentCard, gbcB);

        // Top Books Panel
        JPanel topBooksCard = UIUtils.createCardPanel();
        topBooksCard.setLayout(new BorderLayout());
        JPanel topBooksHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topBooksHeader.setOpaque(false);
        JLabel lblTopBooks = new JLabel("Buku Terpopuler");
        lblTopBooks.setFont(DesignSystem.bodyFont(12f, Font.BOLD));
        lblTopBooks.putClientProperty(FlatClientProperties.STYLE,
            "background: #86000d; foreground: #fff; opaque: true");
        lblTopBooks.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JLabel lblNewArrivals = new JLabel("Buku Baru");
        lblNewArrivals.setFont(DesignSystem.bodyFont(12f, Font.PLAIN));
        lblNewArrivals.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DesignSystem.OUTLINE_VARIANT),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        topBooksHeader.add(lblTopBooks);
        topBooksHeader.add(lblNewArrivals);
        topBooksCard.add(topBooksHeader, BorderLayout.NORTH);

        topBooksList = new JPanel();
        topBooksList.setLayout(new BoxLayout(topBooksList, BoxLayout.Y_AXIS));
        topBooksList.setOpaque(false);
        JLabel lblEmptyTopBooks = new JLabel("Memuat buku terpopuler...");
        lblEmptyTopBooks.setFont(DesignSystem.bodyFont(13f, Font.PLAIN));
        lblEmptyTopBooks.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        lblEmptyTopBooks.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        topBooksList.add(lblEmptyTopBooks);
        
        topBooksCard.add(topBooksList, BorderLayout.CENTER);

        gbcB.gridx = 1; gbcB.weightx = 0.30; gbcB.insets = new Insets(0, 10, 0, 0);
        botSection.add(topBooksCard, gbcB);
        
        dashPanel.add(botSection);

        JScrollPane mainScroll = new JScrollPane(dashPanel);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        return mainScroll;
    }

    public void loadData() {
        SwingWorker<DashboardData, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardData doInBackground() {
                DashboardData d = new DashboardData();
                d.borrowed = transactionService.getCountByStatus("Issued");
                d.returned = transactionService.getCountByStatus("Returned");
                d.overdue = transactionService.getOverdueCount();
                d.missing = transactionService.getLostCount();
                d.totalBooks = bookService.getTotalBooksCount();
                d.totalMembers = userService.getTotalMembersCount();
                d.newMembers = userService.getNewMembersCount();
                d.pendingFees = fineService.getTotalPendingFees();
                d.overdueHistory = transactionService.getOverdueHistory(5);
                d.recentCheckouts = transactionService.getRecentCheckouts(6);
                d.topBooks = bookService.getTopBooks(3);
                d.checkoutStats = transactionService.getCheckoutStats();
                return d;
            }

            @Override
            protected void done() {
                try {
                    DashboardData d = get();
                    
                    // Populate KPI Grid
                    kpiGrid.removeAll();
                    kpiGrid.add(UIUtils.createKPICard("Buku Dipinjam", String.valueOf(d.borrowed), "", true));
                    kpiGrid.add(UIUtils.createKPICard("Buku Dikembalikan", String.valueOf(d.returned), "", true));
                    kpiGrid.add(UIUtils.createKPICard("Buku Terlambat", String.valueOf(d.overdue), "", false));
                    kpiGrid.add(UIUtils.createKPICard("Buku Hilang", String.valueOf(d.missing), "", false));
                    kpiGrid.add(UIUtils.createKPICard("Total Buku", String.valueOf(d.totalBooks), "", true));
                    kpiGrid.add(UIUtils.createKPICard("Total Anggota", String.valueOf(d.totalMembers), "", true));
                    kpiGrid.add(UIUtils.createKPICard("Anggota Baru", String.valueOf(d.newMembers), "", true));
                    kpiGrid.add(UIUtils.createKPICard("Denda Belum Dibayar", String.format("Rp %,.0f", d.pendingFees), "", false));
                    kpiGrid.revalidate();
                    kpiGrid.repaint();

                    // Populate Chart
                    org.jfree.data.time.TimeSeries borrowedSeries = new org.jfree.data.time.TimeSeries("Dipinjam");
                    org.jfree.data.time.TimeSeries returnedSeries = new org.jfree.data.time.TimeSeries("Dikembalikan");
                    
                    if (d.checkoutStats == null || d.checkoutStats.isEmpty()) {
                        borrowedSeries.add(new org.jfree.data.time.Day(new java.util.Date()), 0);
                        returnedSeries.add(new org.jfree.data.time.Day(new java.util.Date()), 0);
                    } else {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        for (DailyStats stat : d.checkoutStats) {
                            try {
                                java.util.Date date = sdf.parse(stat.date);
                                borrowedSeries.add(new org.jfree.data.time.Day(date), stat.borrowed);
                                returnedSeries.add(new org.jfree.data.time.Day(date), stat.returned);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    
                    org.jfree.data.time.TimeSeriesCollection dataset = new org.jfree.data.time.TimeSeriesCollection();
                    dataset.addSeries(borrowedSeries);
                    dataset.addSeries(returnedSeries);
                    
                    org.jfree.chart.JFreeChart chart = org.jfree.chart.ChartFactory.createTimeSeriesChart(
                        "", "", "", dataset, true, true, false
                    );
                    
                    // Populate Chart
                    chart.setBackgroundPaint(DesignSystem.SURFACE_CONTAINER_LOWEST);
                    org.jfree.chart.plot.XYPlot plot = chart.getXYPlot();
                    plot.setBackgroundPaint(DesignSystem.SURFACE_CONTAINER_LOWEST);
                    plot.setOutlineVisible(false);
                    plot.setDomainGridlinesVisible(false);
                    plot.setRangeGridlinePaint(DesignSystem.SURFACE_CONTAINER_HIGH);

                    org.jfree.chart.renderer.xy.XYSplineRenderer renderer = new org.jfree.chart.renderer.xy.XYSplineRenderer();
                    renderer.setSeriesPaint(0, DesignSystem.PRIMARY);          // borrowed → brand red
                    renderer.setSeriesStroke(0, new BasicStroke(2.5f));
                    renderer.setSeriesPaint(1, DesignSystem.TERTIARY);         // returned → academic blue
                    renderer.setSeriesStroke(1, new BasicStroke(2.5f));
                    plot.setRenderer(renderer);

                    chart.getLegend().setFrame(org.jfree.chart.block.BlockBorder.NONE);
                    chart.getLegend().setBackgroundPaint(DesignSystem.SURFACE_CONTAINER_LOWEST);
                    
                    org.jfree.chart.axis.ValueAxis xAxis = plot.getDomainAxis();
                    xAxis.setAxisLineVisible(false);
                    xAxis.setTickMarksVisible(false);
                    if (xAxis instanceof org.jfree.chart.axis.DateAxis) {
                        ((org.jfree.chart.axis.DateAxis) xAxis).setDateFormatOverride(new java.text.SimpleDateFormat("EEE"));
                    }
                    
                    org.jfree.chart.axis.ValueAxis yAxis = plot.getRangeAxis();
                    yAxis.setAxisLineVisible(false);
                    yAxis.setTickMarksVisible(false);
                    
                    org.jfree.chart.ChartPanel chartPanel = new org.jfree.chart.ChartPanel(chart);
                    chartPanel.setBackground(DesignSystem.SURFACE_CONTAINER_LOWEST);
                    
                    BorderLayout layout = (BorderLayout) chartCard.getLayout();
                    java.awt.Component centerComp = layout.getLayoutComponent(BorderLayout.CENTER);
                    if (centerComp != null) chartCard.remove(centerComp);
                    
                    chartCard.add(chartPanel, BorderLayout.CENTER);
                    chartCard.revalidate();
                    chartCard.repaint();

                    // Populate Overdue Table
                    overdueModel.setRowCount(0);
                    for (Map<String, Object> row : d.overdueHistory) {
                        overdueModel.addRow(new Object[]{
                            row.get("memberId"), row.get("title"), row.get("isbn"),
                            row.get("dueDate"), row.get("fine")
                        });
                    }

                    // Populate Recent Table
                    recentModel.setRowCount(0);
                    for (Transaction tx : d.recentCheckouts) {
                        recentModel.addRow(new Object[]{
                            "#" + tx.getUserId(), String.valueOf(tx.getBookId()),
                            tx.getBookTitle(), tx.getMemberName(),
                            tx.getIssueDate(), tx.getReturnDate() == null ? "-" : tx.getReturnDate()
                        });
                    }

                    // Populate Top Books
                    topBooksList.removeAll();
                    if (d.topBooks.isEmpty()) {
                        JLabel lblEmpty = new JLabel("Belum ada peminjaman.");
                        lblEmpty.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground");
                        lblEmpty.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                        topBooksList.add(lblEmpty);
                    } else {
                        for (Book b : d.topBooks) {
                            topBooksList.add(UIUtils.createTopBookItem(b.getTitle(), b.getAuthor()));
                        }
                    }
                    topBooksList.revalidate();
                    topBooksList.repaint();

                } catch (Exception e) {
                    e.printStackTrace();
                    kpiGrid.removeAll();
                    kpiGrid.add(new JLabel("Gagal memuat data dasbor."));
                    kpiGrid.revalidate();
                    kpiGrid.repaint();
                }
            }
        };
        worker.execute();
    }

    private class DashboardData {
        int borrowed;
        int returned;
        int overdue;
        int missing;
        int totalBooks;
        int totalMembers;
        int newMembers;
        double pendingFees;
        List<DailyStats> checkoutStats;
        List<Map<String, Object>> overdueHistory;
        List<Transaction> recentCheckouts;
        List<Book> topBooks;
    }

    private class ScrollablePanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
