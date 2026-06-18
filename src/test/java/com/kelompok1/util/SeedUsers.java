package com.kelompok1.util;

public class SeedUsers {
    public static void main(String[] args) {
        String[] names = {
            "Budi Santoso", "Siti Aminah", "Rina Kartika", "Andi Wijaya",
            "Dewi Lestari", "Eko Prasetyo", "Fitriani Rahma", "Gilang Ramadhan",
            "Hana Pertiwi", "Indra Gunawan", "Joko Susilo", "Kiki Amalia"
        };
        
        String[] phones = {
            "081234567890", "081345678901", "081456789012", "081567890123",
            "081678901234", "081789012345", "081890123456", "081901234567",
            "082012345678", "082123456789", "082234567890", "082345678901"
        };

        String hash = PasswordUtil.hashPassword("rahasia123");

        for (int i = 0; i < names.length; i++) {
            String fullName = names[i];
            String[] parts = fullName.split(" ");
            String username = parts[0].toLowerCase() + "_" + (100 + i);
            String email = parts[0].toLowerCase() + "." + parts[1].toLowerCase() + "@example.com";
            String status = i % 5 == 0 ? "Suspended" : "Active";
            String memberCode = "MEM-" + String.format("%04d", 100 + i);

            System.out.println("INSERT IGNORE INTO users (username, password_hash, full_name, email, phone, role, status, member_code) VALUES ('" + username + "', '" + hash + "', '" + fullName + "', '" + email + "', '" + phones[i] + "', 'Member', '" + status + "', '" + memberCode + "');");
        }
    }
}
