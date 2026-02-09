/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package view;
import controller.KontakController;
import java.io.*;
import model.Kontak;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sudirwo
 */
public class PengelolaanKontakFrame extends javax.swing.JFrame {

    private DefaultTableModel model;
    private KontakController controller;
    
    /**
     * Creates new form PengelolaanKontakFrame
     */
    public PengelolaanKontakFrame() {
        initComponents();
        
        controller = new KontakController();
    
        // Ganti "No" menjadi "ID"
        model = new DefaultTableModel(new String[]{"ID", "Nama", "Nomor Telepon", "Kategori"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Agar user tidak bisa edit manual di tabel
            }
        };
        tblKontak.setModel(model);
        // Menyembunyikan kolom ID (index 0) dari pandangan user, tapi data tetap ada di memory
        tblKontak.getColumnModel().getColumn(0).setMinWidth(0);
        tblKontak.getColumnModel().getColumn(0).setMaxWidth(0);
        tblKontak.getColumnModel().getColumn(0).setWidth(0);
        loadContacts();
    }
    
    private File getUniqueFile(File file) {
        String path = file.getAbsolutePath();
        String extension = "";
        String name = path;

        int idx = path.lastIndexOf('.');
        if (idx > 0) {
            extension = path.substring(idx); // mengambil .csv
            name = path.substring(0, idx);   // mengambil nama tanpa ekstensi
        }

        File newFile = file;
        int count = 1;
        while (newFile.exists()) {
            newFile = new File(name + " (" + count + ")" + extension);
            count++;
        }
        return newFile;
    }
    
    private void loadContacts() {
        // Gunakan SwingWorker agar proses baca DB tidak membuat UI 'hang'
        try {
            model.setRowCount(0);
            List<Kontak> contacts = controller.getAllContacts();
            for (Kontak contact : contacts) {
                model.addRow(new Object[]{
                    contact.getId(), // AMBIL ID DARI DATABASE
                    contact.getNama(),
                    contact.getNomorTelepon(),
                    contact.getKategori()
                });
            }
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }
        private void showError(String message) {
            JOptionPane.showMessageDialog(this, message, "Error",
            JOptionPane.ERROR_MESSAGE);
        }  
        
        private void addContact() {
            String nama = txtNama.getText().trim();
            String nomorTelepon = txtNomorTelepon.getText().trim();
            String kategori = (String) cmbKategori.getSelectedItem();

            if (!validatePhoneNumber(nomorTelepon)) return;

            try {
                if (controller.isDuplicatePhoneNumber(nomorTelepon, null)) {
                    JOptionPane.showMessageDialog(this, "Nomor sudah ada!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                controller.addContact(nama, nomorTelepon, kategori);
                loadContacts(); // <--- WAJIB PANGGIL INI supaya tabel update otomatis
                clearInputFields();
                JOptionPane.showMessageDialog(this, "Berhasil disimpan!");
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        }
        private boolean validatePhoneNumber(String phoneNumber) {
            // Memastikan nomor telepon tidak kosong atau hanya berisi spasi
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nomor telepon tidak boleh kosong.", "Validasi Gagal", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Regex \\d+ memastikan hanya karakter 0-9 yang diizinkan
            if (!phoneNumber.matches("\\d+")) { 
                JOptionPane.showMessageDialog(this, "Nomor telepon hanya boleh berisi angka.", "Validasi Gagal", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Validasi panjang karakter antara 8 hingga 15
            if (phoneNumber.length() < 8 || phoneNumber.length() > 15) { 
                JOptionPane.showMessageDialog(this, "Nomor telepon harus memiliki panjang antara 8 hingga 15 karakter.", "Validasi Gagal", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            return true;
        }
        private void clearInputFields() {
            txtNama.setText("");
            txtNomorTelepon.setText("");
            cmbKategori.setSelectedIndex(0);
        }
        
        private void editContact() {
            int selectedRow = tblKontak.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Pilih baris dulu!");
                return;
            }

            // Mengambil ID asli dari kolom 0
            int id = (int) model.getValueAt(selectedRow, 0); 
            String nama = txtNama.getText();
            String nomor = txtNomorTelepon.getText();
            String kategori = (String) cmbKategori.getSelectedItem();

            try {
                controller.updateContact(id, nama, nomor, kategori);
                JOptionPane.showMessageDialog(this, "Berhasil diupdate!");
                loadContacts(); // Refresh tabel
            } catch (SQLException e) {
                showError(e.getMessage());
            }
        }
        private void populateInputFields(int selectedRow) {
            // Ambil data dari JTable
            String nama = model.getValueAt(selectedRow, 1).toString();
            String nomorTelepon = model.getValueAt(selectedRow, 2).toString();
            String kategori = model.getValueAt(selectedRow, 3).toString();
            // Set data ke komponen input
            txtNama.setText(nama);
            txtNomorTelepon.setText(nomorTelepon);
            cmbKategori.setSelectedItem(kategori);
        }
        
        private void deleteContact() {
            int selectedRow = tblKontak.getSelectedRow();

            if (selectedRow != -1) {
                // AMBIL ID DARI KOLOM 0
                int id = (int) model.getValueAt(selectedRow, 0); 

                int confirm = JOptionPane.showConfirmDialog(this, "Yakin hapus kontak ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        // Proses Hapus via Controller
                        controller.deleteContact(id);

                        // REFRESH DATA
                        loadContacts();
                        clearInputFields();
                        JOptionPane.showMessageDialog(this, "Kontak berhasil dihapus!");
                    } catch (SQLException e) {
                        showError("Gagal menghapus: " + e.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Pilih kontak yang akan dihapus!");
            }
        }
        
        private void searchContact() {
            String keyword = txtPencarian.getText().trim();
            if (!keyword.isEmpty()) {
                try {
                    List<Kontak> contacts = controller.searchContacts(keyword);
                    model.setRowCount(0); // Bersihkan tabel
                    for (Kontak contact : contacts) {
                        model.addRow(new Object[]{
                            contact.getId(),
                            contact.getNama(),
                            contact.getNomorTelepon(),
                            contact.getKategori()
                        });
                    }
                    if (contacts.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Tidak ada kontak ditemukan.");
                    }
                } catch (SQLException ex) {
                    showError(ex.getMessage());
                }
            } else {
                loadContacts();
            }
        }
        
        private void exportToCSV() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Simpan Kontak ke CSV");

            // 1. SET NAMA DEFAULT (misal: kontak.csv)
            fileChooser.setSelectedFile(new File("kontak.csv"));

            // 2. SET FILTER CSV
            javax.swing.filechooser.FileNameExtensionFilter filter = 
                new javax.swing.filechooser.FileNameExtensionFilter("CSV Files (*.csv)", "csv");
            fileChooser.setFileFilter(filter);

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                // Memastikan ekstensi .csv tertulis
                if (!file.getName().toLowerCase().endsWith(".csv")) {
                    file = new File(file.getAbsolutePath() + ".csv");
                }

                // Cek duplikasi nama file agar tidak replace (Auto (1), (2), dst)
                file = getUniqueFile(file);

                try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                    pw.println("ID,Nama,Nomor Telepon,Kategori"); // Header

                    for (int i = 0; i < model.getRowCount(); i++) {
                        pw.println(String.format("\"%s\",\"%s\",\"%s\",\"%s\"",
                            model.getValueAt(i, 0),
                            model.getValueAt(i, 1),
                            model.getValueAt(i, 2),
                            model.getValueAt(i, 3)));
                    }
                    JOptionPane.showMessageDialog(this, "Data berhasil diekspor ke: " + file.getName());
                } catch (IOException e) {
                    showError("Gagal ekspor: " + e.getMessage());
                }
            }
        }
        private void importFromCSV() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Pilih File CSV Kontak");

            // SET FILTER CSV agar file jenis lain (exe, txt, pdf) tersembunyi
            javax.swing.filechooser.FileNameExtensionFilter filter = 
                new javax.swing.filechooser.FileNameExtensionFilter("CSV Files (*.csv)", "csv");
            fileChooser.setFileFilter(filter);
            fileChooser.setAcceptAllFileFilterUsed(false); // Memaksa user hanya melihat CSV

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    br.readLine(); // Lewati header
                    int count = 0;

                    while ((line = br.readLine()) != null) {
                        // Regex parser untuk menangani koma di dalam nama
                        String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                        if (data.length >= 4) {
                            String nama = data[1].replace("\"", "");
                            String nomor = data[2].replace("\"", "");
                            String kategori = data[3].replace("\"", "");

                            if (!controller.isDuplicatePhoneNumber(nomor, null)) {
                                controller.addContact(nama, nomor, kategori);
                                count++;
                            }
                        }
                    }
                    loadContacts();
                    JOptionPane.showMessageDialog(this, count + " data berhasil diimpor!");
                } catch (Exception e) {
                    showError("Gagal impor: " + e.getMessage());
                }
            }
        }
        private void showCSVGuide() {
            String guideMessage = "Format CSV untuk impor data:\n" +
            "- Header wajib: ID, Nama, Nomor Telepon, Kategori\n" +
            "- ID dapat kosong (akan diisi otomatis)\n" +
            "- Nama dan Nomor Telepon wajib diisi\n" +
            "- Contoh isi file CSV:\n" +
            " 1, Andi, 08123456789, Teman\n" +
            " 2, Budi Doremi, 08567890123, Keluarga\n\n" +
            "Pastikan file CSV sesuai format sebelum melakukan impor.";
            JOptionPane.showMessageDialog(this, guideMessage, "Panduan Format CSV", JOptionPane.INFORMATION_MESSAGE);
        }
        private boolean validateCSVHeader(String header) {
            return header != null &&
            header.trim().equalsIgnoreCase("ID,Nama,Nomor Telepon,Kategori");
        }
        

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel1 = new java.awt.Panel();
        lblJudul = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        cmbKategori = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblKontak = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtNama = new javax.swing.JTextField();
        txtNomorTelepon = new javax.swing.JTextField();
        txtPencarian = new javax.swing.JTextField();
        btnTambah = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        btnImport = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Aplikasi Pengelolaan Kontak");

        lblJudul.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        lblJudul.setText("APLIKASI PENGELOLAAN KONTAK");
        panel1.add(lblJudul);

        getContentPane().add(panel1, java.awt.BorderLayout.NORTH);

        cmbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Keluarga", "Teman", "Kantor" }));

        tblKontak.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblKontak.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblKontakMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblKontak);

        jLabel3.setText("Nama Kontak");

        jLabel4.setText("Nomor Telepon");

        jLabel5.setText("Kategori");

        jLabel1.setText("Pencarian");

        txtNama.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNamaActionPerformed(evt);
            }
        });

        txtPencarian.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtPencarianKeyTyped(evt);
            }
        });

        btnTambah.setText("Tambah");
        btnTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahActionPerformed(evt);
            }
        });

        btnEdit.setText("Edit");
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        btnHapus.setText("Hapus");
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });

        btnExport.setText("Export");
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        btnImport.setText("Import");
        btnImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnExport)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnImport))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel3)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel5)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtNomorTelepon)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(btnTambah, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(btnHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 10, Short.MAX_VALUE))
                                    .addComponent(txtNama)
                                    .addComponent(cmbKategori, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(txtPencarian))))))
                .addGap(47, 47, 47))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtNama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNomorTelepon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(btnTambah)
                    .addComponent(btnEdit)
                    .addComponent(btnHapus))
                .addGap(4, 4, 4)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPencarian, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnExport)
                    .addComponent(btnImport))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtNamaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNamaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNamaActionPerformed

    private void btnTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahActionPerformed
        // TODO add your handling code here:
        addContact();
    }//GEN-LAST:event_btnTambahActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // TODO add your handling code here:
        editContact();
    }//GEN-LAST:event_btnEditActionPerformed

    private void tblKontakMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblKontakMouseClicked
        // TODO add your handling code here:
        int selectedRow = tblKontak.getSelectedRow();
        if (selectedRow != -1) {
            populateInputFields(selectedRow);
        }
    }//GEN-LAST:event_tblKontakMouseClicked

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        // TODO add your handling code here:
        deleteContact();
    }//GEN-LAST:event_btnHapusActionPerformed

    private void txtPencarianKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPencarianKeyTyped
        // TODO add your handling code here:
        searchContact();
    }//GEN-LAST:event_txtPencarianKeyTyped

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        // TODO add your handling code here:
        exportToCSV();
    }//GEN-LAST:event_btnExportActionPerformed

    private void btnImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportActionPerformed
        // TODO add your handling code here:
        importFromCSV();
    }//GEN-LAST:event_btnImportActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PengelolaanKontakFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PengelolaanKontakFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PengelolaanKontakFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PengelolaanKontakFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PengelolaanKontakFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnImport;
    private javax.swing.JButton btnTambah;
    private javax.swing.JComboBox<String> cmbKategori;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblJudul;
    private java.awt.Panel panel1;
    private javax.swing.JTable tblKontak;
    private javax.swing.JTextField txtNama;
    private javax.swing.JTextField txtNomorTelepon;
    private javax.swing.JTextField txtPencarian;
    // End of variables declaration//GEN-END:variables
}
