package com.kelompok1.ui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.kelompok1.model.Models.DailyStats;
import com.kelompok1.model.Models.Book;
import com.kelompok1.model.Models.Transaction;
import com.kelompok1.service.Services.BookService;
import com.kelompok1.service.Services.FineService;
import com.kelompok1.service.Services.TransactionService;
import com.kelompok1.service.Services.UserService;
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

    private JPanel circulationGrid;
    private JPanel generalGrid;
    private javax.swing.table.DefaultTableModel overdueModel;
    private javax.swing.table.DefaultTableModel recentModel;

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

        // 1. KPI Grids (Circulation and General data grouped separately with subtitles)
        JPanel kpiWrapper = new JPanel();
        kpiWrapper.setLayout(new BoxLayout(kpiWrapper, BoxLayout.Y_AXIS));
        kpiWrapper.setOpaque(false);
        kpiWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Section 1: Aktivitas Sirkulasi
        JLabel lblCircTitle = new JLabel("AKTIVITAS SIRKULASI");
        lblCircTitle.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblCircTitle.setForeground(DesignSystem.PRIMARY);
        lblCircTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        lblCircTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiWrapper.add(lblCircTitle);
        
        circulationGrid = new JPanel(new GridLayout(1, 4, 16, 16));
        circulationGrid.setOpaque(false);
        circulationGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        circulationGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel loadCirc = new JLabel("Memuat sirkulasi...");
        loadCirc.setFont(DesignSystem.bodyFont(12f, Font.ITALIC));
        loadCirc.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        circulationGrid.add(loadCirc);
        kpiWrapper.add(circulationGrid);
        
        kpiWrapper.add(Box.createVerticalStrut(20));
        
        // Section 2: Data Koleksi & Anggota
        JLabel lblGenTitle = new JLabel("KOLEKSI & ANGGOTA");
        lblGenTitle.setFont(DesignSystem.bodyFont(11f, Font.BOLD));
        lblGenTitle.setForeground(DesignSystem.TERTIARY);
        lblGenTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        lblGenTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiWrapper.add(lblGenTitle);
        
        generalGrid = new JPanel(new GridLayout(1, 4, 16, 16));
        generalGrid.setOpaque(false);
        generalGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        generalGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel loadGen = new JLabel("Memuat data perpustakaan...");
        loadGen.setFont(DesignSystem.bodyFont(12f, Font.ITALIC));
        loadGen.setForeground(DesignSystem.ON_SURFACE_VARIANT);
        generalGrid.add(loadGen);
        kpiWrapper.add(generalGrid);
        
        dashPanel.add(kpiWrapper);
        dashPanel.add(Box.createVerticalStrut(20));
 
        // Mouse wheel listener to propagate scroll events from nested scrollable areas to the parent dashboard scroll pane
        java.awt.event.MouseWheelListener bubbleScroll = e -> {
            Component source = (Component) e.getSource();
            Container parent = source.getParent();
            while (parent != null && !(parent instanceof JScrollPane)) {
                parent = parent.getParent();
            }
            if (parent != null) {
                Container mainScroll = parent.getParent();
                while (mainScroll != null && !(mainScroll instanceof JScrollPane)) {
                    mainScroll = mainScroll.getParent();
                }
                if (mainScroll != null) {
                    mainScroll.dispatchEvent(javax.swing.SwingUtilities.convertMouseEvent(source, e, mainScroll));
                }
            }
        };

        // 2. Middle Section (Overdue Table only, stretched full width)
        JPanel overdueCard = UIUtils.createCardPanel();
        overdueCard.setLayout(new BorderLayout());
        overdueCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        overdueCard.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));
        
        JLabel lblOverdueTitle = new JLabel("Riwayat Keterlambatan");
        lblOverdueTitle.putClientProperty(FlatClientProperties.STYLE, "font: bold +2");
        lblOverdueTitle.setBorder(BorderFactory.createEmptyBorder(15, 4, 10, 4));
        overdueCard.add(lblOverdueTitle, BorderLayout.NORTH);
        
        String[] colOverdue = {"ID Anggota", "Judul", "ISBN", "Tenggat Waktu", "Denda"};
        overdueModel = new javax.swing.table.DefaultTableModel(new Object[][]{}, colOverdue) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tblOverdue = UIUtils.createStyledTable(new Object[][]{}, colOverdue);
        tblOverdue.setModel(overdueModel);
        JScrollPane scrollOverdue = new JScrollPane(tblOverdue);
        scrollOverdue.setBorder(BorderFactory.createEmptyBorder());
        scrollOverdue.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollOverdue.addMouseWheelListener(bubbleScroll);
        tblOverdue.addMouseWheelListener(bubbleScroll);
        overdueCard.add(scrollOverdue, BorderLayout.CENTER);
 
        overdueCard.setPreferredSize(new Dimension(getWidth(), 300));
        overdueCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));
        dashPanel.add(overdueCard);
        dashPanel.add(Box.createVerticalStrut(20));
 
        // 3. Bottom Section (Recent Checkouts, stretched full width)
        JPanel recentCard = UIUtils.createCardPanel();
        recentCard.setLayout(new BorderLayout());
        recentCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        recentCard.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));
        
        JPanel recentHeader = new JPanel(new BorderLayout());
        recentHeader.setOpaque(false);
        recentHeader.setBorder(BorderFactory.createEmptyBorder(15, 4, 10, 4));
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
        recentModel = new javax.swing.table.DefaultTableModel(new Object[][]{}, colRecent) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tblRecent = UIUtils.createStyledTable(new Object[][]{}, colRecent);
        tblRecent.setModel(recentModel);
        JScrollPane scrollRecent = new JScrollPane(tblRecent);
        scrollRecent.setBorder(BorderFactory.createEmptyBorder());
        scrollRecent.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollRecent.addMouseWheelListener(bubbleScroll);
        tblRecent.addMouseWheelListener(bubbleScroll);
        recentCard.add(scrollRecent, BorderLayout.CENTER);
 
        recentCard.setPreferredSize(new Dimension(getWidth(), 300));
        recentCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));
        dashPanel.add(recentCard);

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
                    
                    // Populate Circulation Grid
                    circulationGrid.removeAll();
                    circulationGrid.add(UIUtils.createKPICard("Buku Dipinjam", String.valueOf(d.borrowed), "Aktif", true));
                    circulationGrid.add(UIUtils.createKPICard("Buku Dikembalikan", String.valueOf(d.returned), "Selesai", true));
                    circulationGrid.add(UIUtils.createKPICard("Buku Terlambat", String.valueOf(d.overdue), "Perlu Tindakan", false));
                    circulationGrid.add(UIUtils.createKPICard("Buku Hilang", String.valueOf(d.missing), "Total Hilang", false));
                    circulationGrid.revalidate();
                    circulationGrid.repaint();
 
                    // Populate General Grid
                    generalGrid.removeAll();
                    generalGrid.add(UIUtils.createKPICard("Total Buku", String.valueOf(d.totalBooks), "Unit Koleksi", true));
                    generalGrid.add(UIUtils.createKPICard("Total Anggota", String.valueOf(d.totalMembers), "Terdaftar", true));
                    generalGrid.add(UIUtils.createKPICard("Anggota Baru", String.valueOf(d.newMembers), "Bulan Ini", true));
                    generalGrid.add(UIUtils.createKPICard("Denda Belum Dibayar", String.format("Rp %,.0f", d.pendingFees), "Tagihan Aktif", false));
                    generalGrid.revalidate();
                    generalGrid.repaint();
 
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
 

 
                } catch (Exception e) {
                    e.printStackTrace();
                    circulationGrid.removeAll();
                    circulationGrid.add(new JLabel("Gagal memuat sirkulasi."));
                    circulationGrid.revalidate();
                    circulationGrid.repaint();
                    
                    generalGrid.removeAll();
                    generalGrid.add(new JLabel("Gagal memuat data."));
                    generalGrid.revalidate();
                    generalGrid.repaint();
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
