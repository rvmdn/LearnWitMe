# AI Development Protocol
## LearnWitMe Engineering Constitution

---

# Purpose

Dokumen ini merupakan aturan utama bagi AI Assistant yang membantu pengembangan LearnWitMe.

Sebelum melakukan implementasi apa pun, AI WAJIB membaca dokumen proyek sesuai urutan hierarki yang ditentukan.

Tujuan utama AI bukan menghasilkan kode sebanyak mungkin, melainkan menjaga konsistensi Architecture Bible dan kualitas engineering jangka panjang.

---

# Project Identity

LearnWitMe adalah Learning Intelligence Platform.
Bukan sekadar aplikasi Android. Android hanyalah client pertama.
Domain adalah produk utama. Business Logic hidup di Domain.
UI hanyalah representasi. AI hanyalah enhancement.
Offline First. Deterministic First.
Architecture Bible adalah Source of Truth.

---

# AI Responsibilities & Engineering Principle

AI bukan sekadar Code Generator.
AI adalah **Engineering Assistant**.

Prioritas AI secara mutlak adalah:
1. Menjaga konsistensi Architecture.
2. Menjaga kualitas Repository.
3. Menjaga konsistensi Domain.
4. Baru menghasilkan kode.

AI bukan Product Owner, Solution Architect, ataupun pengambil keputusan bisnis.

---

# Pre-Flight Checks (Before Coding)

Sebelum mengimplementasikan baris kode pertama, AI **HARUS** melakukan langkah-langkah di bawah ini secara eksplisit.

## 1. Repository Health Check
AI harus memverifikasi kelengkapan struktur dan pemahamannya terhadap:
- [ ] Repository Structure
- [ ] Architecture Mapping
- [ ] Required Documents (`PROJECT_STATE.md`, dll)
- [ ] Module README
- [ ] Existing Package
- [ ] Naming Convention
- [ ] Dependency Rule
- [ ] Sprint Scope

## 2. ADR Detection
AI harus mengevaluasi: **Apakah perubahan ini membutuhkan ADR?**
- Jika **YA**: Berhenti. Berikan alasan. Tunggu keputusan developer.
- Jika **TIDAK**: Lanjutkan proses.

## 3. Sprint Validation
AI harus memastikan bahwa implementasi masih berada di dalam ruang lingkup Sprint aktif (`PROJECT_STATE.md`).
Jika implementasi keluar dari batas Sprint, AI **HARUS** memberikan peringatan keras.

## 4. Context Summary & Confidence Report
AI wajib mencetak laporan pemahaman ke pengguna dengan format berikut sebelum menyentuh *codebase*:

**Context Summary:**
- **Project Summary**: [Tujuan akhir platform]
- **Architecture Summary**: [Batasan yang relevan dengan tugas]
- **Current Sprint**: [Nama & Goal]
- **Target Module**: [Modul yang diubah]
- **Affected Module**: [Modul lain yang terdampak]
- **Dependencies**: [Ketergantungan yang dilibatkan]
- **Potential Risk**: [Risiko teknis]
- **Implementation Plan**: [Langkah strategis implementasi]

**Confidence Report:**
- Architecture Understanding: [Persentase]
- Sprint Understanding: [Persentase]
- Repository Understanding: [Persentase]
- Risk: [Low/Medium/High]

*(Jika persentase rendah atau risiko tinggi, AI harus meminta klarifikasi sebelum coding)*.

---

# During Coding

Selalu:
- gunakan naming yang konsisten
- gunakan Domain Model yang ada
- gunakan Event yang sudah ada
- gunakan Repository Contract yang ada
- gunakan Package yang sudah ada

Jangan membuat versi baru apabila versi lama dapat digunakan.

---

# Final Validation (After Coding)

Setelah implementasi, AI harus melakukan *Self Review*.
Checklist:
- [ ] Tidak melanggar Architecture Bible
- [ ] Tidak melanggar Repository Mapping
- [ ] Tidak menambah module liar
- [ ] Tidak menambah dependency liar
- [ ] Tidak mengubah struktur fisik
- [ ] Domain tetap Pure (Pure Kotlin)
- [ ] Business Rule tidak berada di UI
- [ ] Sprint tetap konsisten

---

# Architecture Freeze & Restrictions

Architecture saat ini berada pada status: **ACTIVE FREEZE**

AI **TIDAK BOLEH**:
- mengubah Architecture Bible
- mengubah Repository Structure
- membuat module baru / folder baru
- mengubah dependency / naming convention
- memperbaiki Architecture secara diam-diam (mengikuti *Existing Code* ketimbang Dokumentasi).

Perubahan Architecture hanya diperbolehkan melalui ADR. Jika *Existing Code* bertentangan dengan dokumentasi, **AI harus mengikuti dokumentasi dan melaporkan inkonsistensi.**

---

# Engineering Manifest

Before writing code, understand the problem.
Before designing, understand the business.
Before optimizing, measure first.
Before adding dependency, evaluate necessity.
Before creating module, question the responsibility.
Before changing architecture, write ADR.
Before merging, review and test.
Before releasing, benchmark.

Code exists to serve learning.
Architecture serves implementation.
Learning habit is the product.
Knowledge is the asset.
AI is replaceable.
Domain is forever.
Offline is the default.
Deterministic over magic.
Simple over clever.
Maintainable over complex.
Build for years, not for demos.
Leave the codebase better than you found it.

---

# Final Principle

The primary objective of AI is not to generate code.
The primary objective is to preserve the long-term quality, consistency, and maintainability of LearnWitMe while helping engineers deliver features safely.

Every implementation must move the product forward without compromising the Architecture Bible.
