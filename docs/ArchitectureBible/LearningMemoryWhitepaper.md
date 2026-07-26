# Learning Memory Whitepaper
## The Intellectual Property and Cognitive Engine of LearnWitMe

---

## 1. Vision

### Mengapa Learning Memory Ada?
Metode pembelajaran tradisional dan sebagian besar sistem manajemen pembelajaran (LMS) modern memperlakukan proses belajar secara linier dan transaksional. Pengguna menyelesaikan materi, lulus kuis, mendapatkan tanda centang hijau, dan sistem menganggap materi tersebut telah "dikuasai" selamanya. Namun, otak manusia tidak bekerja dengan cara demikian. Otak secara konstan menyaring informasi tidak aktif untuk menghemat energi kognitif melalui mekanisme pemangkasan sinaptik (*synaptic pruning*).

**Learning Memory** hadir sebagai perpanjangan kognitif (*cognitive extension*) pengguna di dalam platform LearnWitMe. Komponen ini dirancang untuk merepresentasikan secara dinamis, akurat, dan real-time kondisi memori manusia terhadap materi yang dipelajari.

### Masalah yang Diselesaikan
1.  **The Illusion of Competence (Ilusi Kompetensi)**: Pengguna sering merasa menguasai materi sesaat setelah membacanya, padahal memori tersebut baru tersimpan di *short-term memory* dan akan hilang dalam beberapa jam.
2.  **Over-learning dan Under-learning**: Belajar terlalu sering pada topik yang sudah stabil membuang waktu (*inefficient learning*), sedangkan terlambat mengulang topik yang mulai terlupakan akan memutus rantai pembentukan koneksi saraf (*interrupted consolidation*).
3.  **Static Curriculum**: Kurikulum kaku yang tidak memedulikan kecepatan lupa individual pengguna yang unik.

### Perbedaan dari Progress Biasa
*   **Progress Biasa**: Bersifat linier, searah, bertambah secara akumulatif (misalnya: "Materi SQL Select selesai 100%"), dan tidak pernah berkurang meskipun pengguna tidak pernah menyentuh materi tersebut selama satu tahun.
*   **Learning Memory**: Bersifat dinamis, dua arah (dapat bertambah kuat atau meluruh turun), memperhitungkan waktu, tingkat retensi kognitif, dan pola perilaku interaksi pengguna secara real-time.

---

## 2. Learning Memory Philosophy

Untuk merancang mesin kognitif yang andal, platform harus membedakan dengan tegas istilah-istilah berikut:

```
[Progress] ──> [Knowledge] ──> [Retention] ──> [Mastery]
  (Aktivitas)     (Struktur)     (Daya Ingat)     (Kecakapan)
       │              │               │                │
       └──────────────┴───────┬───────┴────────────────┘
                              ▼
                      [Learning Memory]
```

*   **Progress (Kemajuan)**: Ukuran kuantitatif dari aktivitas fisik yang diselesaikan (misalnya jumlah modul dibaca, jumlah soal dijawab). Progress bersifat searah dan tidak pernah mundur.
*   **Knowledge (Pengetahuan)**: Struktur informasi kognitif yang telah dipetakan secara konseptual di dalam sistem (misalnya keterkaitan antara topik Database dengan konsep SQL Indexing).
*   **Retention (Retensi)**: Probabilitas saat ini bahwa pengguna dapat mengingat kembali (*recall*) informasi tertentu tanpa bantuan stimulus luar pada titik waktu spesifik. Retensi meluruh seiring waktu mengikuti fungsi peluruhan waktu (*decay*).
*   **Mastery (Penguasaan)**: Kemampuan pengguna untuk mengaplikasikan pengetahuan dalam konteks baru dengan akurat, cepat, dan tingkat kesalahan minimal.
*   **Memory (Memori)**: Representasi hidup yang mencakup kekuatan retensi kognitif saat ini, stabilitas penyimpanan memori jangka panjang, sejarah interaksi, dan kerentanan lupa pengguna terhadap suatu unit pengetahuan. **Learning Memory** adalah super-set dari seluruh komponen di atas.

---

## 3. Human Memory Model

Sistem kognitif LearnWitMe memodelkan siklus hidup ingatan manusia melalui alur konseptual berikut:

```
[Learn] ──> [Understand] ──> [Practice] ──> [Recall]
                 │                               │
                 │ (Gagal Paham)                 │ (Lupa)
                 ▼                               ▼
             [Review] <───────────────────── [Forget]
                 │
                 ▼ (Sukses)
            [Remember] ──> [Master]
```

### 1. Learn (Mempelajari)
Pengguna pertama kali bersentuhan dengan informasi mentah (membaca kartu pengetahuan atau menyimak materi baru). Informasi ini berada di ranah *sensory memory* dan *working memory*.

### 2. Understand (Memahami)
Otak menghubungkan informasi baru dengan skema pengetahuan yang sudah ada (*schema integration*). Di tahap ini, pengguna memahami "mengapa" dan "bagaimana" konsep tersebut bekerja. Kegagalan di tahap ini menghambat pembentukan memori jangka panjang yang sehat.

### 3. Practice (Mempraktikkan)
Pengguna menerapkan informasi tersebut melalui aktivitas aktif seperti menjawab kuis pendek atau memecahkan skenario dunia nyata. Latihan ini memicu penguatan plastisitas sinaptik (*long-term potentiation*).

### 4. Recall (Mengingat Kembali)
Pengguna berupaya memanggil ingatan secara aktif tanpa melihat catatan. Ini adalah proses *active recall* yang memicu restrukturisasi koneksi saraf agar ingatan menjadi lebih kuat.

### 5. Forget (Lupa)
Informasi yang tidak diakses atau tidak diperkuat secara berkala akan mengalami peluruhan (*decay*). Hubungan sinaptik melonggar, dan ingatan bergeser dari kondisi mudah diakses (*high retrieval strength*) menjadi sulit diakses.

### 6. Review (Mengulas Kembali)
Proses peninjauan kembali materi saat memori berada di ambang kelupaan. Melakukan ulasan tepat sebelum lupa memberikan dampak penguatan memori terbesar (*desirable difficulty*).

### 7. Remember (Mengingat)
Setelah ulasan sukses, memori dikonsolidasikan kembali dengan stabilitas penyimpanan yang lebih tinggi. Kecepatan meluruh selanjutnya akan berjalan jauh lebih lambat dari sebelumnya.

### 8. Master (Menguasai)
Kondisi di mana ingatan konseptual telah tersimpan stabil di memori jangka panjang (*long-term memory*) dengan stabilitas sangat tinggi, sehingga pengguna dapat memanggilnya secara instan meskipun tanpa ulasan dalam waktu lama.

---

## 4. The Forgetting Curve

Model kognitif LearnWitMe didasarkan pada pengembangan teori *Forgetting Curve* milik Hermann Ebbinghaus, yang diperluas dengan variabel perilaku belajar modern.

```
Retensi (R)
100% |─────┐
     |      \ (Ulasan Pertama)
     |       \   ┌───────────┐
     |        \ /             \ (Ulasan Kedua - Peluruhan Lebih Lambat)
     |         *               \─────┐
     |        /                       \
     |       /                         \
  0% └──────┴───────────────────────────┴──── Waktu
```

### Variabel Kognitif Utama:
*   **Retention (Kekuatan Retensi)**: Probabilitas teoretis bahwa pengguna dapat mengingat suatu informasi saat ini (skala 0% hingga 100%).
*   **Decay (Peluruhan)**: Kecepatan penurunan kekuatan retensi seiring berjalannya waktu tanpa adanya ulasan aktif. Kecepatan peluruhan ini bervariasi untuk setiap pengguna dan dipengaruhi oleh tingkat kesulitan inheren materi.
*   **Recovery (Pemulihan)**: Penguatan kembali kekuatan retensi setelah ulasan yang sukses. Tingkat pemulihan akan lebih tinggi apabila pengguna berhasil mengingat materi yang dinilai sulit (*high difficulty recall*).
*   **Confidence (Kepercayaan Diri)**: Persepsi subjektif pengguna terhadap seberapa baik mereka menguasai materi tersebut. Ini digunakan untuk menyeimbangkan metrik objektif sistem.
*   **Review Interval (Interval Ulasan)**: Jarak waktu optimal antar sesi ulasan untuk suatu kartu pengetahuan. Setiap ulasan yang sukses akan memperpanjang interval ulasan berikutnya secara eksponensial.
*   **Review Quality (Kualitas Ulasan)**: Nilai performa selama pengulangan (misalnya: akurasi jawaban pertama, kecepatan menjawab, penggunaan petunjuk). Kualitas ulasan rendah menunjukkan pemulihan memori yang lemah.
*   **Memory Stability (Stabilitas Memori)**: Daya tahan teoretis memori terhadap peluruhan. Semakin sering pengguna berhasil melakukan ulasan dengan interval yang membesar, semakin stabil memori tersebut dan semakin lambat peluruhannya.

---

## 5. Memory Signals

Untuk memperkirakan kondisi memori pengguna secara akurat tanpa mengandalkan tes formal yang terus-menerus, sistem mengamati sekumpulan sinyal interaksi pasif (*implicit signals*) dan aktif (*explicit signals*):

1.  **Reading Time (Waktu Membaca)**: Berapa lama waktu yang dihabiskan pengguna pada kartu pengetahuan dibandingkan dengan estimasi waktu baca teoritis. Waktu baca terlalu cepat dapat mengindikasikan *skimming* (pemahaman rendah), sedangkan terlalu lama dapat menunjukkan materi yang terlalu kompleks.
2.  **Quiz Accuracy (Akurasi Kuis)**: Rasio jawaban benar pada percobaan pertama untuk kartu pengetahuan terkait. Ini adalah sinyal penentu performa objektif.
3.  **Hint Usage (Penggunaan Petunjuk)**: Menunjukkan bahwa pengguna memiliki retensi parsial namun membutuhkan stimulus penunjang untuk memicu *recall*. Sinyal ini menurunkan bobot pemulihan memori dibandingkan recall mandiri yang bersih.
4.  **Wrong Answer Pattern (Pola Jawaban Salah)**: Menganalisis apakah jawaban salah pengguna berpola (misalnya memilih distraktor yang sangat mirip dengan jawaban benar vs. menebak secara acak). Pola ini mendeteksi miskonsepsi vs. kelupaan total.
5.  **Review Count (Jumlah Ulasan)**: Akumulasi berapa kali suatu kartu pengetahuan telah diulas. Menentukan tingkat kedewasaan stabilitas memori.
6.  **Last Review (Waktu Ulasan Terakhir)**: Jarak waktu sejak interaksi penguatan terakhir pada materi tersebut.
7.  **Streak (Rantai Konsistensi)**: Jumlah keberhasilan berturut-turut dalam sesi kuis terjadwal tanpa melakukan kesalahan pada topik tersebut.
8.  **Skip Frequency (Frekuensi Melewati)**: Seberapa sering pengguna memutuskan melewati ulasan kartu tertentu. Sinyal ini dapat mengindikasikan kelelahan belajar (*learning fatigue*) atau ketakutan terhadap materi sulit (*desirable difficulty avoidance*).
9.  **Confidence (Skor Efikasi Diri)**: Kepercayaan diri subjektif pengguna saat diuji.
10. **Self Rating (Penilaian Mandiri)**: Feedback langsung pengguna setelah membaca kartu pengetahuan tentang seberapa sulit mereka mengingat materi tersebut.

---

## 6. Memory State Machine

Kondisi kognitif setiap konsep di dalam memori pengguna dikelola melalui mesin status terpadu berikut:

```
                  ┌─────────[ NEW ]─────────┐
                  │                         │
                  ▼                         ▼
            [ LEARNING ] ─────────────> [ FORGOTTEN ]
                  │   ▲                     ▲   │
                  │   │ (Gagal Ulasan)      │   │ (Ulasan Sukses)
  (Ulasan Sukses) │   │                     │   ▼
                  ▼   │               [ RECOVERED ]
           [ REMEMBERED ] ──────────────────┘
                  │
                  ▼ (Ulasan Berkelanjutan & Stabil)
             [ MASTERED ]
```

### Status Kognitif:
*   **NEW**: Kartu pengetahuan baru saja terdaftar di dalam kurikulum pengguna namun belum pernah dipelajari atau disentuh.
*   **LEARNING**: Kartu sedang aktif dipelajari, dicoba dalam latihan kuis pertama kali, namun belum mencapai stabilitas memori jangka pendek.
*   **REMEMBERED**: Pengguna berhasil mengulang materi pada waktu optimal dengan akurasi yang baik. Retensi tinggi, stabilitas memori mulai terbentuk.
*   **MASTERED**: Materi telah diulang beberapa kali dengan interval yang membesar dan konsisten berhasil. Peluruhan berjalan sangat lambat; materi dikonsolidasikan ke memori jangka panjang.
*   **FORGOTTEN**: Kekuatan retensi merosot di bawah ambang batas kritis (threshold teoretis, misalnya 60%) karena peluruhan waktu atau kegagalan berulang pada kuis ulasan.
*   **RECOVERED**: Materi yang sempat terlupakan berhasil diingat kembali melalui sesi ulasan remedial. Status ini membawa riwayat kegagalan sebelumnya agar sistem tetap waspada pada peluruhan di masa depan.

---

## 7. Adaptive Review

Mekanisme penentuan jadwal belajar didasarkan pada konsep adaptasi individu kognitif tanpa menggunakan rumus matematika statis (seperti Spaced Repetition standar).

*   **Penentuan Ulasan Berikutnya**: Waktu optimal untuk melakukan ulasan ditentukan saat kekuatan retensi diperkirakan berada di angka kritis (misal $R \approx 80\%-85\%$). Belajar di titik ini meminimalkan usaha pengulangan namun memaksimalkan konsolidasi memori.
*   **Prioritas & Urgensi**: Ketika beberapa topik memerlukan ulasan pada hari yang sama, sistem menghitung prioritas berdasarkan tingkat kerawanan lupa (*imminent forgetting risk*) dan relevansi topik tersebut terhadap materi baru yang akan dipelajari selanjutnya.
*   **Review Frequency (Frekuensi Ulasan)**: Untuk topik dengan tingkat kerumitan tinggi atau riwayat sering terlupakan (*recurrent memory lapses*), frekuensi ulasan akan otomatis diperketat dan dijadwalkan ulang lebih cepat.

---

## 8. Weak Topic Detection

Sistem kognitif LearnWitMe melacak tingkat kepahaman secara hirarkis dari tingkat kartu pengetahuan individual, sub-topik, hingga topik utama untuk memetakan "titik lemah" kognitif pengguna:

```
[Topik Utama] ──> [Sub-Topik] ──> [KnowledgeCard 1] (Stabil)
                              ──> [KnowledgeCard 2] (Lemah - Sering Lupa)
```

1.  **Topik Lemah (Weak Topics)**: Topik yang memiliki tingkat akurasi rendah pada kuis ulasan pertama kali, sering menggunakan petunjuk, atau memiliki frekuensi kegagalan *recall* yang tinggi pada sub-sub topiknya.
2.  **Topik Kuat (Strong Topics)**: Topik yang dikuasai dengan cepat, memiliki kestabilan memori tinggi, dan memiliki riwayat pengulangan sukses tanpa jeda kegagalan.
3.  **Topik Mulai Lupa (Decaying Topics)**: Topik yang saat ini memiliki tingkat retensi yang merosot mendekati batas kritis karena sudah lama tidak dibuka atau diuji ulang.
4.  **Topik Stabil (Stable Topics)**: Topik dengan stabilitas memori jangka panjang sangat tinggi (Mastered) yang tidak memerlukan intervensi belajar dalam waktu dekat.

---

## 9. Memory Analytics

Sistem melacak metrik kognitif berikut sebagai representasi ilmiah status memori pengguna:

*   **Retention Score (Skor Retensi)**: Estimasi probabilitas teoretis pengguna untuk sukses mengingat kembali materi kuis saat ini (skala 0.0 - 1.0).
*   **Mastery Score (Skor Penguasaan)**: Pengukuran komposit yang menggabungkan akurasi jawaban kuis, kecepatan respons, konsistensi penyelesaian tanpa bantuan petunjuk, dan keaktifan durasi pemecahan masalah (skala 0.0 - 1.0).
*   **Confidence Score (Skor Kepercayaan Diri)**: Indikator efikasi kognitif subjektif pengguna yang dinilai melalui feedback mandiri.
*   **Decay Rate (Laju Peluruhan)**: Kecepatan teoretis penurunan ingatan pengguna per satuan waktu untuk topik tertentu. Dipengaruhi oleh faktor kelupaan individual dan kerumitan materi.
*   **Review Success (Tingkat Keberhasilan Ulasan)**: Rasio keberhasilan kuis ulasan dibandingkan dengan total sesi ulasan terjadwal.
*   **Recovery Rate (Kecepatan Pemulihan)**: Kecepatan memori kembali stabil ke status "Remembered" setelah mengalami kelupaan (Forgotten).
*   **Memory Age (Umur Memori)**: Waktu akumulasi sejak pertama kali konsep ini bertransisi dari status `New` menjadi aktif di dalam sistem.

---

## 10. Recommendation Relation

Learning Memory bertindak sebagai *single source of truth* kognitif yang memengaruhi seluruh keputusan engine lain di dalam platform:

```
                   ┌───────────────────┐
                   │  LEARNING MEMORY  │
                   └─────────┬─────────┘
        ┌────────────────────┼────────────────────┐
        ▼                    ▼                    ▼
[Recommendation]         [Quiz]               [Roadmap]
  (Bahan Rekomendasi)      (Soal Relevan)       (Urutan Materi)
```

*   **Recommendation Engine**: Memilih kartu pengetahuan untuk dipelajari hari ini. Mesin akan mendahulukan kartu ulasan yang retensinya kritis sebelum menyarankan materi baru untuk menjaga konsistensi pengetahuan fondasi.
*   **Quiz Engine**: Menghasilkan kuis harian yang fokus pada pertanyaan konseptual dari topik-topik yang berstatus `Learning`, `Forgotten`, atau topik `Remembered` yang mendekati masa peluruhan.
*   **Feed Engine**: Menyesuaikan tampilan beranda atau feed pengguna agar menampilkan ringkasan kilat (*flashcards*) dari topik yang dinilai "Mulai Lupa".
*   **Roadmap Engine**: Menghambat kemajuan pengguna pada lintasan belajar jika prasyarat konsep sebelumnya dinilai tidak memenuhi batas aman retensi kognitif (menghindari pemaksaan maju materi ketika fondasi rapuh).

---

## 11. AI Relation

LearnWitMe menegaskan batasan tanggung jawab kecerdasan buatan (AI) di dalam sistem:

1.  **AI Bukan Pemilik Memori**: Kecerdasan Buatan (LLM atau model AI lainnya) tidak mengelola status memori, retensi, atau jadwal ulasan pengguna. Status memori dikelola secara deterministik dan eksklusif oleh **Domain Model murni platform**.
2.  **Peran AI**: AI berperan sebagai asisten operasional yang bertugas menerjemahkan tujuan bebas pengguna (*free-text parsing*), menyusun variasi pertanyaan kuis kreatif berdasarkan parameter kesulitan yang diminta sistem, atau memberikan analogi penjelasan baru pada kartu remedial jika pengguna mengalami kesulitan pemahaman berulang.
3.  **Domain Sovereignty**: Data relasi memori kognitif tetap menjadi kedaulatan domain model internal tanpa membagikan atau memprosesnya melalui API pihak ketiga untuk menjaga privasi pengguna dan performa sistem.

---

## 12. Future Evolution

Pengembangan model kognitif Learning Memory direncanakan melalui tahapan berikut:

### Versi 1: Deterministic Cognitive Model (Current Sprint Scope)
*   Pemetaan struktur domain murni (`LearningMemory`, `ReviewSchedule`, `KnowledgeScore`).
*   Pengolahan sinyal dasar (waktu baca, akurasi kuis, interval waktu ulasan).
*   Penjadwalan Spaced Repetition dasar dengan status transisi yang jelas.

### Versi 2: Per-User Behavioral Adaptation (Personalized Curves)
*   Sistem mulai membedakan kurva peluruhan untuk tipe materi yang berbeda (misalnya: memori untuk sintaksis pemrograman meluruh lebih cepat dibanding memori untuk konsep logika pemrograman).
*   Penggunaan metrik pola kesalahan menjawab (*Wrong Answer Pattern*) secara terstruktur.

### Versi 3: Context-Aware Cognitive Memory (Biometric & Environmental Integration)
*   Integrasi data eksternal seperti waktu belajar dalam sehari, tingkat kelelahan belajar kognitif (*cognitive load feedback*), dan efektivitas rentang konsentrasi pengguna.
*   Penerapan kurva peluruhan adaptif kolaboratif (*collaborative filtering of memory decay*) berdasarkan agregasi kesulitan materi antar pengguna yang memiliki pola belajar serupa.
