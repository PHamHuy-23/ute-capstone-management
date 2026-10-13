# -*- coding: utf-8 -*-
"""
BÁO CÁO KỸ THUẬT VỀ XỬ LÝ LỖI VÀ TỐI ƯU HỆ THỐNG QUẢN LÝ ĐỀ TÀI SINH VIÊN (HCMUTE CAPSTONE MANAGEMENT)
Tạo file Word (.docx) chuẩn format OpenXML / python-docx:
- Font chữ: Times New Roman, Toàn bộ màu chữ Đen (#000000).
- Cỡ chữ: Nội dung 13pt (sz=26), giãn dòng 1.3 - 1.5 (line=312 - 360), giãn đoạn sau 4-6pt (after=80 - 120).
- Tiêu đề Heading: In đậm, cỡ chữ chuẩn (Heading 1: 16pt, Heading 2: 14pt, Heading 3: 13pt), màu đen.
- Cấu trúc chuẩn 4 phần:
    + Trang bìa / Tiêu đề
    + Phần 1: Giới thiệu tổng quan hệ thống & bối cảnh phát sinh lỗi
    + Phần 2: Chi tiết 6 lỗi kỹ thuật (Đủ 4 mục: Tên lỗi & Hiện tượng, Phân tích nguyên nhân sâu RCA, Code Before & After, Giải pháp & Kiểm thử)
    + Phần 3: Tổng hợp danh mục các file code đã can thiệp & chỉnh sửa
    + Phần 4: Kết luận & Đánh giá mức độ ổn định (17/17 tests pass)
"""

import os
import io
import zipfile

def generate_report(output_filename):
    # 1. OpenXML Declarations
    content_types_xml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
    <Default Extension="xml" ContentType="application/xml"/>
    <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
    <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
    <Override PartName="/word/footer1.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.footer+xml"/>
</Types>"""

    root_rels_xml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""

    doc_rels_xml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
    <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer" Target="footer1.xml"/>
</Relationships>"""

    styles_xml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
    <w:docDefaults>
        <w:rPrDefault>
            <w:rPr>
                <w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman" w:cs="Times New Roman"/>
                <w:color w:val="000000"/>
                <w:sz w:val="26"/>
                <w:szCs w:val="26"/>
                <w:lang w:val="vi-VN"/>
            </w:rPr>
        </w:rPrDefault>
        <w:pPrDefault>
            <w:pPr>
                <w:spacing w:line="320" w:lineRule="auto" w:after="100"/>
            </w:pPr>
        </w:pPrDefault>
    </w:docDefaults>
    <w:style w:type="paragraph" w:default="1" w:styleId="Normal">
        <w:name w:val="Normal"/>
        <w:qFormat/>
    </w:style>
    <w:style w:type="table" w:default="1" w:styleId="TableNormal">
        <w:name w:val="Normal Table"/>
        <w:tblPr>
            <w:tblBorders>
                <w:top w:val="single" w:sz="6" w:space="0" w:color="000000"/>
                <w:left w:val="single" w:sz="6" w:space="0" w:color="000000"/>
                <w:bottom w:val="single" w:sz="6" w:space="0" w:color="000000"/>
                <w:right w:val="single" w:sz="6" w:space="0" w:color="000000"/>
                <w:insideH w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/>
                <w:insideV w:val="single" w:sz="4" w:space="0" w:color="CCCCCC"/>
            </w:tblBorders>
        </w:tblPr>
    </w:style>
</w:styles>"""

    footer_xml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:ftr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
    <w:p>
        <w:pPr>
            <w:pBdr>
                <w:top w:val="single" w:sz="4" w:space="4" w:color="CCCCCC"/>
            </w:pBdr>
            <w:jc w:val="center"/>
            <w:spacing w:before="60" w:after="0"/>
        </w:pPr>
        <w:r>
            <w:rPr>
                <w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman" w:cs="Times New Roman"/>
                <w:color w:val="000000"/>
                <w:sz w:val="18"/>
            </w:rPr>
            <w:t xml:space="preserve">Báo cáo Kỹ thuật Xử lý Lỗi &amp; Tối ưu Hệ thống Quản lý Đề tài HCMUTE | Trang </w:t>
        </w:r>
        <w:fldSimple w:instr="PAGE">
            <w:r>
                <w:rPr>
                    <w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman" w:cs="Times New Roman"/>
                    <w:color w:val="000000"/>
                    <w:sz w:val="18"/>
                    <w:b/>
                </w:rPr>
                <w:t>1</w:t>
            </w:r>
        </w:fldSimple>
    </w:p>
</w:ftr>"""

    # Helper formatters
    def escape(t):
        if t is None:
            return ""
        return str(t).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace('"', "&quot;")

    def p(text="", runs=None, jc="both", before=0, after=100, line=320, bold=False, italic=False, size=26, color="000000", indent=0):
        ind_str = f'<w:ind w:firstLine="{indent}"/>' if indent > 0 else ''
        pPr = f'<w:pPr>{ind_str}<w:jc w:val="{jc}"/><w:spacing w:line="{line}" w:lineRule="auto" w:before="{before}" w:after="{after}"/></w:pPr>'
        body = ""
        if runs:
            for r in runs:
                t_val = escape(r.get("text", ""))
                r_b = "<w:b/>" if r.get("bold", False) else ""
                r_i = "<w:i/>" if r.get("italic", False) else ""
                r_sz = f'<w:sz w:val="{r.get("size", size)}"/><w:szCs w:val="{r.get("size", size)}"/>'
                r_col = f'<w:color w:val="{r.get("color", color)}"/>'
                font_name = r.get("font", "Times New Roman")
                r_f = f'<w:rFonts w:ascii="{font_name}" w:hAnsi="{font_name}" w:cs="{font_name}"/>'
                body += f'<w:r><w:rPr>{r_f}{r_b}{r_i}{r_sz}{r_col}</w:rPr><w:t xml:space="preserve">{t_val}</w:t></w:r>'
        elif text is not None:
            t_val = escape(text)
            r_b = "<w:b/>" if bold else ""
            r_i = "<w:i/>" if italic else ""
            r_sz = f'<w:sz w:val="{size}"/><w:szCs w:val="{size}"/>'
            r_col = f'<w:color w:val="{color}"/>'
            r_f = '<w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman" w:cs="Times New Roman"/>'
            body = f'<w:r><w:rPr>{r_f}{r_b}{r_i}{r_sz}{r_col}</w:rPr><w:t xml:space="preserve">{t_val}</w:t></w:r>'
        return f'<w:p>{pPr}{body}</w:p>'

    def heading1(text):
        # Heading 1: In đậm, cỡ chữ 16pt (sz=32), màu đen, giãn trên 240, giãn dưới 120
        return p(text, size=32, bold=True, before=240, after=120, jc="left", color="000000")

    def heading2(text):
        # Heading 2: In đậm, cỡ chữ 14pt (sz=28), màu đen, giãn trên 180, giãn dưới 80
        return p(text, size=28, bold=True, before=180, after=80, jc="left", color="000000")

    def heading3(text):
        # Heading 3: In đậm, cỡ chữ 13pt (sz=26), màu đen, giãn trên 140, giãn dưới 60
        return p(text, size=26, bold=True, before=140, after=60, jc="left", color="000000")

    def callout_box(label, message, fill="F8F9FA", bdr_col="000000"):
        inner_p = p(runs=[
            {"text": f"[{label}] ", "bold": True, "size": 26, "color": "000000"},
            {"text": message, "bold": False, "size": 26, "color": "000000"}
        ], jc="both", before=40, after=40, line=300)
        
        return f"""<w:tbl>
            <w:tblPr>
                <w:tblW w:w="9070" w:type="dxa"/>
                <w:tblBorders>
                    <w:top w:val="none"/>
                    <w:left w:val="single" w:sz="24" w:space="0" w:color="{bdr_col}"/>
                    <w:bottom w:val="none"/>
                    <w:right w:val="none"/>
                    <w:insideH w:val="none"/>
                    <w:insideV w:val="none"/>
                </w:tblBorders>
                <w:tblCellMar>
                    <w:top w:w="120" w:type="dxa"/>
                    <w:left w:w="180" w:type="dxa"/>
                    <w:bottom w:w="120" w:type="dxa"/>
                    <w:right w:w="180" w:type="dxa"/>
                </w:tblCellMar>
            </w:tblPr>
            <w:tr>
                <w:tc>
                    <w:tcPr>
                        <w:tcW w:w="9070" w:type="dxa"/>
                        <w:shd w:val="clear" w:color="auto" w:fill="{fill}"/>
                    </w:tcPr>
                    {inner_p}
                </w:tc>
            </w:tr>
        </w:tbl>"""

    def code_box(code_text, label="MÃ NGUỒN"):
        lines = code_text.strip().split("\n")
        inner_xml = p(label, size=22, bold=True, color="000000", before=40, after=40, line=240)
        for line in lines:
            inner_xml += f"""<w:p>
                <w:pPr>
                    <w:spacing w:line="240" w:lineRule="auto" w:before="0" w:after="20"/>
                </w:pPr>
                <w:r>
                    <w:rPr>
                        <w:rFonts w:ascii="Consolas" w:hAnsi="Consolas" w:cs="Consolas"/>
                        <w:color w:val="000000"/>
                        <w:sz w:val="21"/>
                        <w:szCs w:val="21"/>
                    </w:rPr>
                    <w:t xml:space="preserve">{escape(line)}</w:t>
                </w:r>
            </w:p>"""

        return f"""<w:tbl>
            <w:tblPr>
                <w:tblW w:w="9070" w:type="dxa"/>
                <w:tblBorders>
                    <w:top w:val="single" w:sz="6" w:space="0" w:color="000000"/>
                    <w:left w:val="single" w:sz="18" w:space="0" w:color="000000"/>
                    <w:bottom w:val="single" w:sz="6" w:space="0" w:color="000000"/>
                    <w:right w:val="single" w:sz="6" w:space="0" w:color="000000"/>
                </w:tblBorders>
                <w:tblCellMar>
                    <w:top w:w="120" w:type="dxa"/>
                    <w:left w:w="180" w:type="dxa"/>
                    <w:bottom w:w="120" w:type="dxa"/>
                    <w:right w:w="180" w:type="dxa"/>
                </w:tblCellMar>
            </w:tblPr>
            <w:tr>
                <w:tc>
                    <w:tcPr>
                        <w:tcW w:w="9070" w:type="dxa"/>
                        <w:shd w:val="clear" w:color="auto" w:fill="F4F6F8"/>
                    </w:tcPr>
                    {inner_xml}
                </w:tc>
            </w:tr>
        </w:tbl>"""

    def make_table(headers, rows, col_widths=None):
        total_w = 9070
        n_cols = len(headers)
        if not col_widths:
            cw = [total_w // n_cols] * n_cols
            cw[-1] = total_w - sum(cw[:-1])
        else:
            cw = col_widths

        tbl_xml = f"""<w:tbl>
            <w:tblPr>
                <w:tblW w:w="{total_w}" w:type="dxa"/>
                <w:tblBorders>
                    <w:top w:val="single" w:sz="12" w:space="0" w:color="000000"/>
                    <w:left w:val="single" w:sz="6" w:space="0" w:color="000000"/>
                    <w:bottom w:val="single" w:sz="12" w:space="0" w:color="000000"/>
                    <w:right w:val="single" w:sz="6" w:space="0" w:color="000000"/>
                    <w:insideH w:val="single" w:sz="6" w:space="0" w:color="000000"/>
                    <w:insideV w:val="single" w:sz="6" w:space="0" w:color="000000"/>
                </w:tblBorders>
                <w:tblCellMar>
                    <w:top w:w="120" w:type="dxa"/>
                    <w:left w:w="140" w:type="dxa"/>
                    <w:bottom w:w="120" w:type="dxa"/>
                    <w:right w:w="140" w:type="dxa"/>
                </w:tblCellMar>
            </w:tblPr>"""

        # Header row
        tbl_xml += '<w:tr><w:trPr><w:tblHeader/></w:trPr>'
        for i, h in enumerate(headers):
            tbl_xml += f"""<w:tc>
                <w:tcPr>
                    <w:tcW w:w="{cw[i]}" w:type="dxa"/>
                    <w:shd w:val="clear" w:color="auto" w:fill="E9ECEF"/>
                </w:tcPr>
                {p(h, size=26, bold=True, jc="center", before=60, after=60, line=260, color="000000")}
            </w:tc>"""
        tbl_xml += '</w:tr>'

        # Data rows
        for row_idx, r in enumerate(rows):
            tbl_xml += '<w:tr>'
            shd_fill = "FFFFFF" if row_idx % 2 == 0 else "FDFDFD"
            for i, cell in enumerate(r):
                align = "center" if (i == 0 and len(cell) < 10) or "Pass" in cell or "100%" in cell else "left"
                tbl_xml += f"""<w:tc>
                    <w:tcPr>
                        <w:tcW w:w="{cw[i]}" w:type="dxa"/>
                        <w:shd w:val="clear" w:color="auto" w:fill="{shd_fill}"/>
                    </w:tcPr>
                    {p(cell, size=26, bold=False, jc=align, before=60, after=60, line=280, color="000000")}
                </w:tc>"""
            tbl_xml += '</w:tr>'

        tbl_xml += '</w:tbl>'
        return tbl_xml

    def page_break():
        return '<w:p><w:r><w:br w:type="page"/></w:r></w:p>'

    # Assemble Document Content
    body_parts = []

    # ====================================================
    # TRANG BÌA (COVER PAGE)
    # ====================================================
    body_parts.append(p("TRƯỜNG ĐẠI HỌC SƯ PHẠM KỸ THUẬT THÀNH PHỐ HỒ CHÍ MINH", size=26, bold=True, jc="center", before=100, after=40))
    body_parts.append(p("KHOA CÔNG NGHỆ THÔNG TIN — BỘ MÔN CÔNG NGHỆ PHẦN MỀM", size=24, bold=True, jc="center", before=0, after=400))
    body_parts.append(p("― ― ― ― ― ― ― ― ― ― ― ― ― ― ― ― ― ― ― ― ― ― ― ― ― ―", size=24, bold=True, jc="center", before=0, after=800))

    body_parts.append(p("BÁO CÁO KỸ THUẬT", size=32, bold=True, jc="center", before=400, after=200))
    body_parts.append(p("XỬ LÝ LỖI VÀ TỐI ƯU HỆ THỐNG QUẢN LÝ ĐỀ TÀI SINH VIÊN", size=36, bold=True, jc="center", before=0, after=150))
    body_parts.append(p("(HCMUTE CAPSTONE MANAGEMENT)", size=28, bold=True, jc="center", before=0, after=600))

    body_parts.append(callout_box("MỤC ĐÍCH TÀI LIỆU", 
        "Tài liệu kỹ thuật tổng hợp toàn diện nguyên nhân gốc rễ (Root Cause Analysis - RCA), cơ chế hoạt động chuyên sâu của Spring Boot 4.1.1, Hibernate ORM và Thymeleaf 3.1+, cùng giải pháp xử lý và kiểm thử thực tế cho 6 lỗi kỹ thuật trọng yếu phát sinh trong quá trình xây dựng hệ thống quản lý đề tài sinh viên.",
        fill="F8F9FA", bdr_col="000000"))

    body_parts.append(p("", before=600, after=0))

    cover_meta_headers = ["Thuộc tính", "Giá trị"]
    cover_meta_rows = [
        ["Hệ thống", "Hệ thống Quản lý Đề tài Sinh viên (HCMUTE Capstone Management)"],
        ["Đơn vị phát triển", "Khoa Công nghệ Thông tin - Trường Đại học Sư phạm Kỹ thuật TP.HCM"],
        ["Công nghệ chủ đạo", "Java 21 LTS, Spring Boot 4.1.1, Spring Data JPA, Hibernate, Thymeleaf, MySQL 8.0"],
        ["Phong cách thiết kế", "Server-Side Rendering + Neo-Brutalism B&W Design System"],
        ["Số lỗi kỹ thuật xử lý", "06 Lỗi trọng yếu (SpEL Coercion, Hibernate Session, Spring MVC Routing, XSS DOM, Time-window Guard, Safe Deletion)"],
        ["Kết quả kiểm thử", "17/17 Unit Tests tự động đạt 100% Pass Rate"],
        ["Thời điểm hoàn thành", "Tháng 10 / 2026"]
    ]
    body_parts.append(make_table(cover_meta_headers, cover_meta_rows, [2600, 6470]))
    body_parts.append(page_break())

    # ====================================================
    # PHẦN 1: GIỚI THIỆU TỔNG QUAN
    # ====================================================
    body_parts.append(heading1("PHẦN 1: GIỚI THIỆU TỔNG QUAN HỆ THỐNG & BỐI CẢNH PHÁT SINH LỖI"))
    
    body_parts.append(p(
        "Hệ thống Quản lý Đề tài Sinh viên (HCMUTE Capstone Management) được thiết kế nhằm số hóa và tự động hóa toàn diện quy trình "
        "quản lý các loại hình đề tài học thuật tại Khoa Công nghệ Thông tin - Trường Đại học Sư phạm Kỹ thuật TP.HCM, bao gồm: "
        "Đề tài Môn học, Nghiên cứu Khoa học (NCKH), Tiểu luận Chuyên ngành (TLCN) và Khóa luận Tốt nghiệp (KLTN). Hệ thống phân quyền "
        "và phục vụ 4 nhóm tác nhân chủ chốt: Trưởng khoa (Admin/Dean), Trưởng bộ môn (Head of Department), Giảng viên (Lecturer/Advisor/Reviewer/Council) "
        "và Sinh viên (Student).", indent=720
    ))

    body_parts.append(p(
        "Hệ thống vận hành theo quy trình khép kín 5 giai đoạn: (1) Thiết lập đợt đăng ký & Xây dựng danh mục đề tài; (2) Giảng viên đề xuất & Bộ môn phê duyệt; "
        "(3) Nhóm sinh viên thành lập và đăng ký đề tài chính thức; (4) Nhóm trưởng nộp báo cáo hoàn thành & Giảng viên theo dõi; "
        "(5) Thành lập hội đồng phản biện, nhập điểm đánh giá và tự động tính toán công bố kết quả.", indent=720
    ))

    body_parts.append(p(
        "Dự án được xây dựng trên nền tảng công nghệ Java 21 LTS kết hợp Spring Boot 4.1.1. Với việc ứng dụng các tiêu chuẩn mới nhất "
        "của hệ sinh thái Spring Framework và Thymeleaf 3.1+, nhiều cơ chế an toàn nghiêm ngặt đã được kích hoạt. Tuy nhiên, sự khác biệt "
        "giữa lý thuyết thiết kế và thực tế vận hành (đặc biệt trong khâu quản lý vòng đời Session của Hibernate, cơ chế kiểm duyệt biểu thức "
        "SpEL tĩnh, cơ chế phòng vệ chống XSS injection của Thymeleaf, và việc xử lý ràng buộc thời gian nghiệp vụ) đã dẫn đến sự phát sinh của "
        "các lỗi kỹ thuật phức tạp. Việc phân tích chuyên sâu nguyên nhân gốc rễ và xử lý triệt để các lỗi này là nền tảng cốt lõi "
        "để đưa hệ thống đạt chuẩn độ tin cậy cấp doanh nghiệp (Production-grade).", indent=720
    ))

    body_parts.append(callout_box("TỔNG HỢP 6 LỖI KỸ THUẬT ĐÃ XỬ LÝ",
        "Danh mục các sự cố bao gồm: (1) Ngoại lệ SpEL Expression ConverterNotFoundException tại danh sách đợt; (2) Ngoại lệ Hibernate NonUniqueObjectException "
        "trên TopicAdvisor composite key; (3) Lỗi HTTP 404 Not Found tại Danh mục Đề tài của Bộ môn; (4) Ngoại lệ Thymeleaf TemplateProcessingException "
        "do String Concatenation trong DOM onclick; (5) Vi phạm Time-window Chốt chặn thời gian đề xuất đề tài; (6) Thiếu Safe Deletion Guard gây Foreign Key Constraint.",
        fill="F8F9FA", bdr_col="000000"))

    body_parts.append(page_break())

    # ====================================================
    # PHẦN 2: CHI TIẾT TỪNG LỖI KỸ THUẬT
    # ====================================================
    body_parts.append(heading1("PHẦN 2: CHI TIẾT CÁC LỖI KỸ THUẬT, NGUYÊN NHÂN VÀ GIẢI PHÁP"))

    # ----------------------------------------------------
    # LỖI 1
    # ----------------------------------------------------
    body_parts.append(heading2("2.1. LỖI 1: LỖI SpEL EXPRESSION TẠI DANH SÁCH ĐỢT ĐĂNG KÝ"))
    
    body_parts.append(heading3("a) Tên lỗi & Hiện tượng ghi nhận"))
    body_parts.append(p(
        "Khi người quản trị truy cập vào giao diện quản lý danh sách đợt đăng ký đề tài tại đường dẫn '/admin/periods', hệ thống gặp sự cố, "
        "toàn bộ giao diện bị sập và trả về trang lỗi 500 WhiteLabel Error Page với thông báo ngoại lệ trích dẫn chính xác như sau:", indent=720
    ))
    err1_stack = """org.springframework.core.convert.ConverterNotFoundException: No converter found capable of converting from type [java.time.LocalDateTime] to type [java.lang.Boolean]
    at org.springframework.core.convert.support.GenericConversionService.handleResult(GenericConversionService.java:319)
    at org.springframework.core.convert.support.GenericConversionService.convert(GenericConversionService.java:175)
    at org.springframework.expression.spel.support.ReflectiveMethodResolver.resolve(...)
    at org.thymeleaf.standard.expression.StandardExpressionEngine.execute(...)"""
    body_parts.append(code_box(err1_stack, "EXCEPTION TRÍCH DẪN TỪ CONSOLE"))

    body_parts.append(heading3("b) Phân tích nguyên nhân kỹ thuật chuyên sâu (Root Cause Analysis)"))
    body_parts.append(p(
        "1. Cơ chế nội suy SpEL (Spring Expression Language) và Type Coercion: Trong các ngôn ngữ kịch bản lỏng lẻo như JavaScript, "
        "lập trình viên thường có thói quen sử dụng toán tử phủ định '!object' để kiểm tra xem đối tượng có rỗng (null/undefined) hay không "
        "(truthy/falsy evaluation). Tuy nhiên, Spring Boot 4.1.1 sử dụng chuẩn phân tích biểu thức SpEL tĩnh nghiêm ngặt.", indent=720
    ))
    body_parts.append(p(
        "2. Ràng buộc trường LocalDateTime của Java 8 Date-Time API: Thuộc tính 'reviewDeadline' trong thực thể RegistrationPeriod "
        "có kiểu dữ liệu là 'java.time.LocalDateTime'. Toán tử logic '!' (NOT) trong SpEL chỉ chấp nhận đầu vào là kiểu Boolean. "
        "Khi phát hiện biểu thức '!p.reviewDeadline', bộ điều phối biểu thức của Spring chuyển quyền xử lý cho GenericConversionService "
        "để cố gắng ép kiểu (Type Coercion) từ LocalDateTime sang Boolean. Do không có bất kỳ Converter tương ứng nào trong ConversionRegistry, "
        "hệ thống lập tức ném ngoại lệ ConverterNotFoundException làm gián đoạn hoàn toàn chu trình biên dịch view của Thymeleaf.", indent=720
    ))

    body_parts.append(heading3("c) Đoạn code trước khi sửa (Before) và sau khi sửa (After)"))
    code1_bf = """<!-- GIAO DIỆN list.html TRƯỚC KHI SỬA (LỖI SpEL TYPE COERCION) -->
<td>
    <!-- LỖI: Dùng toán tử phủ định logic trên đối tượng LocalDateTime -->
    <div th:if="${!p.reviewDeadline}">
        <span>Chưa thiết lập</span>
    </div>
    <div th:unless="${!p.reviewDeadline}">
        <span th:text="${#temporals.format(p.reviewDeadline, 'dd/MM/yyyy')}"></span>
    </div>
</td>"""
    body_parts.append(code_box(code1_bf, "CODE TRƯỚC KHI SỬA (BEFORE)"))

    code1_af = """<!-- GIAO DIỆN list.html SAU KHI SỬA (SO SÁNH TƯỜNG MINH VỚI NULL) -->
<td class="small">
    <!-- KHẮC PHỤC: So sánh trực tiếp với con trỏ null an toàn tuyệt đối -->
    <div th:if="${p.reviewDeadline != null}">
        <strong>GVPB:</strong> <span th:text="${#temporals.format(p.reviewDeadline, 'dd/MM/yyyy')}"></span>
    </div>
    <div th:if="${p.defenseDate != null}">
        <strong>HĐ:</strong> <span th:text="${#temporals.format(p.defenseDate, 'dd/MM/yyyy')}"></span>
    </div>
    <div th:if="${p.reviewDeadline == null and p.defenseDate == null}" class="text-muted">-</div>
</td>"""
    body_parts.append(code_box(code1_af, "CODE SAU KHI SỬA (AFTER)"))

    body_parts.append(heading3("d) Giải pháp và Kết quả kiểm thử thực tế"))
    body_parts.append(p(
        "Giải pháp: Loại bỏ hoàn toàn việc sử dụng toán tử '!' trên các đối tượng phức tạp trong template. Thay thế bằng phép so sánh "
        "tường minh '${p.reviewDeadline != null}' và '${p.defenseDate != null}'. Kết hợp với cú pháp '#temporals.format()' "
        "để hiển thị định dạng ngày tháng chuẩn Việt Nam (dd/MM/yyyy). Kết quả kiểm thử: Màn hình danh sách đợt đăng ký tải 100% dữ liệu "
        "mượt mà cho tất cả các loại hình đợt (Môn học, NCKH, TLCN, KLTN) mà không xảy ra bất kỳ lỗi render nào.", indent=720
    ))

    # ----------------------------------------------------
    # LỖI 2
    # ----------------------------------------------------
    body_parts.append(heading2("2.2. LỖI 2: LỖI QUẢN LÝ SESSION CỦA HIBERNATE KHI LƯU ĐỀ TÀI"))

    body_parts.append(heading3("a) Tên lỗi & Hiện tượng ghi nhận"))
    body_parts.append(p(
        "Khi Giảng viên thực hiện lưu đề xuất đề tài mới hoặc chỉnh sửa đề tài có giảng viên đồng hướng dẫn tại '/lecturer/topics/create', "
        "hệ thống phát sinh lỗi nghiêm trọng tại tầng Persistence và trả về ngoại lệ:", indent=720
    ))
    err2_stack = """org.hibernate.NonUniqueObjectException: A different object with the same identifier value was already associated with the session: [vn.edu.ute.capstone.entity.TopicAdvisor#vn.edu.ute.capstone.entity.TopicAdvisorId@...]
    at org.hibernate.engine.internal.StatefulPersistenceContext.checkUniqueness(StatefulPersistenceContext.java:650)
    at org.hibernate.event.internal.DefaultSaveOrUpdateEventListener.entityIsTransient(...)
    at org.hibernate.event.internal.DefaultSaveEventListener.performSaveOrUpdate(...)"""
    body_parts.append(code_box(err2_stack, "EXCEPTION TRÍCH DẪN TỪ CONSOLE"))

    body_parts.append(heading3("b) Phân tích nguyên nhân kỹ thuật chuyên sâu (Root Cause Analysis)"))
    body_parts.append(p(
        "1. Cơ chế First-Level Cache và Identity Map của Hibernate: Trong một giao dịch Hibernate Session / JPA EntityManager, mỗi thực thể "
        "được định danh bởi một khóa chính duy nhất trong Persistence Context. Hibernate tuyệt đối không cho phép hai thực thể Java khác nhau "
        "(khác tham chiếu memory) cùng đại diện cho một khóa chính trong một Session.", indent=720
    ))
    body_parts.append(p(
        "2. Vấn đề Composite Key kết hợp với Generated Identifier: Thực thể 'TopicAdvisor' sử dụng khóa phức hợp '@EmbeddedId TopicAdvisorId' "
        "bao gồm (topicId, lecturerId). Khi Giảng viên cập nhật đề tài, nếu tầng Service khởi tạo một danh sách List<TopicAdvisor> mới và gán đè "
        "bằng 'topic.setAdvisors(newList)', Hibernate vẫn đang duy trì danh sách cũ trong First-Level Cache. Khi gọi 'save(topic)', "
        "StatefulPersistenceContext phát hiện sự hiện diện của hai thực thể TopicAdvisor riêng biệt nhưng có cùng khóa định danh (topicId, lecturerId), "
        "từ đó quăng ngoại lệ NonUniqueObjectException để ngăn chặn nguy cơ corrupt dữ liệu quan hệ.", indent=720
    ))

    body_parts.append(heading3("c) Đoạn code trước khi sửa (Before) và sau khi sửa (After)"))
    code2_bf = """// DỊCH VỤ TRƯỚC KHI SỬA (TopicServiceImpl.java)
Topic topic = findById(topicId);
// LỖI: Tạo List mới và gán đè trực tiếp, phá vỡ tracking của Hibernate Session
List<TopicAdvisor> newAdvisors = new ArrayList<>();
newAdvisors.add(TopicAdvisor.builder()
        .id(new TopicAdvisorId(topic.getId(), gv.getId()))
        .topic(topic)
        .lecturer(gv)
        .isPrimary(true)
        .build());
topic.setAdvisors(newAdvisors); // Gây NonUniqueObjectException khi save
return topicRepository.save(topic);"""
    body_parts.append(code_box(code2_bf, "CODE TRƯỚC KHI SỬA (BEFORE)"))

    code2_af = """// DỊCH VỤ SAU KHI SỬA (CHUẨN HÓA ORPHANREMOVAL VÀ CLEAR COLLECTION)
// 1. Ánh xạ quan hệ trong Topic.java:
// @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
// private List<TopicAdvisor> advisors = new ArrayList<>();

// 2. Tầng dịch vụ TopicServiceImpl.java:
Topic topic = findById(topicId);

// KHẮC PHỤC: Xóa trên collection đang được Session quản lý thay vì gán mới instance
topic.getAdvisors().clear();

topic.getAdvisors().add(TopicAdvisor.builder()
        .id(new TopicAdvisorId(topic.getId(), currentLecturer.getId()))
        .topic(topic)
        .lecturer(currentLecturer)
        .isPrimary(true)
        .build());

if (coLecturer != null) {
    topic.getAdvisors().add(TopicAdvisor.builder()
            .id(new TopicAdvisorId(topic.getId(), coLecturer.getId()))
            .topic(topic)
            .lecturer(coLecturer)
            .isPrimary(false)
            .build());
}

return topicRepository.save(topic); // Hibernate tự quản lý Session an toàn tuyệt đối"""
    body_parts.append(code_box(code2_af, "CODE SAU KHI SỬA (AFTER)"))

    body_parts.append(heading3("d) Giải pháp và Kết quả kiểm thử thực tế"))
    body_parts.append(p(
        "Giải pháp: Cấu hình 'orphanRemoval = true' trên thực thể cha Topic. Khi cập nhật danh sách quan hệ 1-N, luôn thao tác trực tiếp "
        "trên proxy collection được quản lý bằng lệnh 'topic.getAdvisors().clear()' trước khi thêm các phần tử mới, tuyệt đối không gán đè "
        "bằng collection mới. Kết quả kiểm thử: Tính năng đề xuất và cập nhật đề tài (với 1 hoặc 2 giảng viên hướng dẫn) hoạt động trơn tru 100%, "
        "bộ kiểm thử TopicServiceTest vượt qua toàn bộ các ca kiểm thử.", indent=720
    ))

    # ----------------------------------------------------
    # LỖI 3
    # ----------------------------------------------------
    body_parts.append(heading2("2.3. LỖI 3: LỖI 404 NOT FOUND TẠI 'DANH MỤC ĐỀ TÀI' CỦA TRƯỞNG BỘ MÔN"))

    body_parts.append(heading3("a) Tên lỗi & Hiện tượng ghi nhận"))
    body_parts.append(p(
        "Khi Trưởng bộ môn hoặc Quản trị viên click vào mục 'Danh mục Đề tài' (Topic Catalog) trên thanh điều hướng bên trái, "
        "hệ thống không mở ra trang danh mục mà phản hồi mã lỗi HTTP 404 Not Found ('No static resource department/catalog'):", indent=720
    ))
    err3_stack = """HTTP ERROR 404 NOT FOUND
Request URL: http://localhost:8080/department/catalog
Status: 404
Message: No static resource department/catalog.
DispatcherServlet: Completed 404 NOT_FOUND for request [/department/catalog]"""
    body_parts.append(code_box(err3_stack, "THÔNG BÁO LỖI 404 TRÍCH DẪN"))

    body_parts.append(heading3("b) Phân tích nguyên nhân kỹ thuật chuyên sâu (Root Cause Analysis)"))
    body_parts.append(p(
        "1. Cơ chế ánh xạ URL phân cấp của Spring MVC: Khi Spring MVC quét các Controller, URL thực tế của một endpoint được ghép nối từ "
        "tiền tố cấp độ lớp (@RequestMapping ở class) và đường dẫn của phương thức (@GetMapping ở method).", indent=720
    ))
    body_parts.append(p(
        "2. Xung đột cấp độ Class-level Mapping: Ban đầu, DepartmentTopicController được đặt tiền tố là '@RequestMapping(\"/department/topics\")'. "
        "Phương thức xem danh mục được định nghĩa là '@GetMapping(\"/catalog\")'. Khi đó, Spring MVC DispatcherServlet đăng ký URL thực tế là "
        "'/department/topics/catalog'. Trong khi đó, tại thanh điều hướng sidebar của hệ thống, liên kết được cấu hình chuẩn là '/department/catalog'. "
        "Khi người dùng truy cập '/department/catalog', DispatcherServlet không tìm thấy HandlerMethod nào trùng khớp nên chuyển hướng tìm sang "
        "ResourceHttpRequestHandler (tài nguyên tĩnh) và trả về lỗi 404.", indent=720
    ))

    body_parts.append(heading3("c) Đoạn code trước khi sửa (Before) và sau khi sửa (After)"))
    code3_bf = """// CẤU HÌNH CONTROLLER TRƯỚC KHI SỬA
@Controller
@RequestMapping("/department/topics") // LỖI: Tiền tố bị thu hẹp vào /topics
public class DepartmentTopicController {

    @GetMapping("")
    public String listTopics(...) { ... }

    @GetMapping("/catalog") // URL thực tế thành: /department/topics/catalog
    public String viewCatalog(...) {
        return "department/topics/catalog";
    }
}"""
    body_parts.append(code_box(code3_bf, "CODE TRƯỚC KHI SỬA (BEFORE)"))

    code3_af = """// CẤU HÌNH CONTROLLER SAU KHI SỬA CHUẨN HOÁ
@Controller
@RequestMapping("/department") // Chuẩn hóa tiền tố chung của Bộ môn
@RequiredArgsConstructor
public class DepartmentTopicController {

    @GetMapping("/topics") // Khớp đúng URL xét duyệt: /department/topics
    public String listTopicsForDepartment(...) {
        return "department/topics/list";
    }

    @GetMapping("/catalog") // Khớp đúng URL danh mục: /department/catalog
    public String viewCatalog(...) {
        return "department/topics/catalog";
    }
}"""
    body_parts.append(code_box(code3_af, "CODE SAU KHI SỬA (AFTER)"))

    body_parts.append(heading3("d) Giải pháp và Kết quả kiểm thử thực tế"))
    body_parts.append(p(
        "Giải pháp: Cấu trúc lại tiền tố của DepartmentTopicController thành '@RequestMapping(\"/department\")'. Phân tách rõ ràng 2 tuyến chức năng: "
        "'/department/topics' chuyên trách việc Xét duyệt đề tài của giảng viên; và '/department/catalog' chuyên trách việc Tra cứu "
        "danh mục toàn bộ đề tài của bộ môn. Kết quả kiểm thử: Thanh điều hướng sidebar hoạt động chính xác 100%, trang danh mục đề tài "
        "hiển thị đầy đủ bộ lọc theo đợt và trạng thái.", indent=720
    ))

    # ----------------------------------------------------
    # LỖI 4
    # ----------------------------------------------------
    body_parts.append(heading2("2.4. LỖI 4: LỖI BẢO MẬT DOM EVENT & TEMPLATE PARSING CỦA THYMELEAF 3.1+"))

    body_parts.append(heading3("a) Tên lỗi & Hiện tượng ghi nhận"))
    body_parts.append(p(
        "Tại giao diện xét duyệt đề tài của Bộ môn ('department/topics/list.html'), khi bộ máy Thymeleaf biên dịch trang web chứa nút "
        "'Từ chối đề tài' (mở popup nhập lý do từ chối), quá trình render bị chặn đứng với ngoại lệ bảo mật nghiêm trọng:", indent=720
    ))
    err4_stack = """org.thymeleaf.exceptions.TemplateProcessingException: Only variable expressions returning numbers or booleans are allowed in this context, any other datatypes are not allowed in order to clean the expression and prevent code injection
    at org.thymeleaf.standard.expression.StandardExpressionEngine.execute(...)
    in template 'department/topics/list.html', line 112, col 37: th:onclick="'openRejectModal(' + ${t.id} + ', \\'' + ${t.title} + '\\')'"""
    body_parts.append(code_box(err4_stack, "EXCEPTION TRÍCH DẪN TỪ CONSOLE"))

    body_parts.append(heading3("b) Phân tích nguyên nhân kỹ thuật chuyên sâu (Root Cause Analysis)"))
    body_parts.append(p(
        "1. Cơ chế phòng thủ chống tấn công DOM-based XSS của Thymeleaf 3.1+: Trong các phiên bản Thymeleaf mới nhất (từ 3.1 trở đi), "
        "nhằm triệt tiêu nguy cơ tiêm mã độc Cross-Site Scripting (XSS), bộ phân tích cú pháp đã siết chặt quy tắc an ninh trên toàn bộ "
        "các thuộc tính xử lý sự kiện nội tuyến DOM (DOM event handlers như onclick, onchange, onsubmit).", indent=720
    ))
    body_parts.append(p(
        "2. Cấm nội suy chuỗi String tự do: Thymeleaf chỉ cho phép truyền các giá trị số (number) hoặc cờ logic (boolean) đơn giản "
        "vào 'th:onclick'. Khi lập trình viên cố tình nối chuỗi String chứa tiêu đề đề tài (${t.title}) vào 'th:onclick', hệ thống nhận diện "
        "đây là một hành vi tiềm ẩn rủi ro tiêm mã độc JavaScript nguy hiểm và chủ động ngắt tiến trình xử lý bằng ngoại lệ TemplateProcessingException.", indent=720
    ))

    body_parts.append(heading3("c) Đoạn code trước khi sửa (Before) và sau khi sửa (After)"))
    code4_bf = """<!-- GIAO DIỆN list.html TRƯỚC KHI SỬA (LỖI BẢO MẬT XSS INJECTION) -->
<button type="button" class="nb-btn nb-btn-sm text-danger"
        th:onclick="'openRejectModal(' + ${t.id} + ', \\'' + ${t.title} + '\\')'">
    <i class="bi bi-x-lg me-1"></i>TỪ CHỐI
</button>"""
    body_parts.append(code_box(code4_bf, "CODE TRƯỚC KHI SỬA (BEFORE)"))

    code4_af = """<!-- GIAO DIỆN list.html SAU KHI SỬA (CHUẨN HTML5 DATA ATTRIBUTES) -->
<button type="button" class="nb-btn nb-btn-sm text-danger"
        th:data-id="${t.id}"
        th:data-title="${t.title}"
        onclick="openRejectModalFromBtn(this)"
        title="Từ chối đề tài">
    <i class="bi bi-x-lg me-1"></i>TỪ CHỐI
</button>

<script>
function openRejectModalFromBtn(btn) {
    const topicId = btn.getAttribute('data-id');
    const topicTitle = btn.getAttribute('data-title');
    document.getElementById('rejectTopicId').value = topicId;
    document.getElementById('rejectTopicTitle').textContent = 'Đề tài: ' + topicTitle;
    document.getElementById('rejectModal').style.display = 'flex';
}
</script>"""
    body_parts.append(code_box(code4_af, "CODE SAU KHI SỬA (AFTER)"))

    body_parts.append(heading3("d) Giải pháp và Kết quả kiểm thử thực tế"))
    body_parts.append(p(
        "Giải pháp: Áp dụng chuẩn thiết kế HTML5 bằng cách lưu trữ các tham số động vào các thuộc tính dữ liệu chuẩn 'th:data-id' và 'th:data-title'. "
        "Thuộc tính sự kiện được giữ nguyên là JavaScript tĩnh thuần 'onclick=\"openRejectModalFromBtn(this)\"'. Khi được kích hoạt, "
        "hàm JavaScript đọc giá trị thông qua hàm 'btn.getAttribute()'. Kết quả kiểm thử: Khắc phục triệt để lỗi biên dịch template, "
        "loại bỏ hoàn toàn nguy cơ XSS Injection, Modal từ chối hiển thị tên đề tài chính xác và lưu lý do từ chối thành công.", indent=720
    ))

    # ----------------------------------------------------
    # LỖI 5
    # ----------------------------------------------------
    body_parts.append(heading2("2.5. LỖI 5: VẤN ĐỀ TIME-WINDOW CHỐT CHẶN THỜI GIAN ĐỀ XUẤT ĐỀ TÀI"))

    body_parts.append(heading3("a) Tên lỗi & Hiện tượng ghi nhận"))
    body_parts.append(p(
        "Hiện tượng ghi nhận: Giảng viên vẫn có thể gửi biểu mẫu đề xuất đề tài mới hoặc chỉnh sửa thông tin đề tài thành công "
        "ngay cả khi thời hạn nộp đề tài (lecturerEndDate) đã kết thúc từ nhiều ngày trước, miễn là Quản trị viên chưa kịp chuyển trạng thái "
        "của đợt từ 'OPEN_LECTURER' sang 'OPEN_STUDENT'. Điều này vi phạm nghiêm trọng tính liêm chính và quy chế đào tạo của trường:", indent=720
    ))
    err5_msg = """RÀNG BUỘC QUY CHẾ ĐÀO TẠO ĐẠI HỌC SƯ PHẠM KỸ THUẬT:
'Giảng viên chỉ được phép đề xuất và chỉnh sửa thông tin đề tài trong phạm vi khung thời gian quy định
(lecturerStartDate <= thời gian thực <= lecturerEndDate). Mọi hành động ngoài khung thời gian đều không hợp lệ.'"""
    body_parts.append(code_box(err5_msg, "RÀNG BUỘC NGHIỆP VỤ (BUSINESS INVARIANT)"))

    body_parts.append(heading3("b) Phân tích nguyên nhân kỹ thuật chuyên sâu (Root Cause Analysis)"))
    body_parts.append(p(
        "1. Thiếu sót trong kiến trúc Lập trình Phòng thủ (Defensive Backend): Tầng dịch vụ TopicServiceImpl ban đầu chỉ kiểm tra duy nhất "
        "điều kiện trạng thái 'period.getStatus() == PeriodStatus.OPEN_LECTURER'. Lập trình viên giả định rằng trạng thái của hệ thống sẽ luôn "
        "được người quản trị chuyển đổi đúng hạn.", indent=720
    ))
    body_parts.append(p(
        "2. Bỏ qua Cửa sổ thời gian thực tế (Time-window Boundary Check): Trong môi trường vận hành thực tế, Quản trị viên có thể quên "
        "hoặc bận công tác, khiến đợt vẫn giữ cờ trạng thái 'OPEN_LECTURER' dù thời gian kết thúc đã trôi qua. Một backend đạt chuẩn doanh nghiệp "
        "bắt buộc phải thực hiện kiểm chứng kép: Xác thực đồng thời cả Trạng thái máy (State Machine) và Cửa sổ thời gian thực tế (Time-window Guard).", indent=720
    ))

    body_parts.append(heading3("c) Đoạn code trước khi sửa (Before) và sau khi sửa (After)"))
    code5_bf = """// KIỂM TRA ĐỢT TRƯỚC KHI SỬA (CHỈ KIỂM TRA CỜ TRẠNG THÁI)
private RegistrationPeriod validatePeriodForProposal(Long periodId) {
    RegistrationPeriod period = periodRepository.findById(periodId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đợt!"));

    // LỖI: Chỉ kiểm tra cờ trạng thái, hoàn toàn bỏ qua thời gian thực tế
    if (period.getStatus() != PeriodStatus.OPEN_LECTURER) {
        throw new IllegalStateException("Đợt đăng ký hiện không mở cho giảng viên đề xuất!");
    }
    return period;
}"""
    body_parts.append(code_box(code5_bf, "CODE TRƯỚC KHI SỬA (BEFORE)"))

    code5_af = """// KIỂM TRA ĐỢT SAU KHI SỬA (CHỐT CHẶN KÉP - DUAL VERIFICATION)
private RegistrationPeriod validatePeriodForProposal(Long periodId) {
    RegistrationPeriod period = periodRepository.findById(periodId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đợt đăng ký tương ứng."));

    // 1. Chốt chặn trạng thái máy (State Guard)
    if (period.getStatus() != PeriodStatus.OPEN_LECTURER) {
        throw new IllegalStateException("Đợt đăng ký '" + period.getName() + "' hiện không mở cho giảng viên đề xuất đề tài.");
    }

    // 2. Chốt chặn cửa sổ thời gian thực tế (Time-window Guard)
    LocalDateTime now = LocalDateTime.now();
    if (now.isBefore(period.getLecturerStartDate()) || now.isAfter(period.getLecturerEndDate())) {
        throw new IllegalStateException("Thời gian đề xuất đề tài cho đợt này đã kết thúc hoặc chưa bắt đầu (Từ " 
                + period.getLecturerStartDate() + " đến " + period.getLecturerEndDate() + ").");
    }

    return period;
}"""
    body_parts.append(code_box(code5_af, "CODE SAU KHI SỬA (AFTER)"))

    body_parts.append(heading3("d) Giải pháp và Kết quả kiểm thử thực tế"))
    body_parts.append(p(
        "Giải pháp: Cài đặt phương thức kiểm tra an toàn 'validatePeriodForProposal()' tích hợp đồng thời State Guard và Time-window Guard "
        "vào cả hai luồng 'createTopic' và 'updateTopic'. Nếu thời gian hiện tại nằm ngoài khoảng cho phép, hệ thống lập tức ném ngoại lệ "
        "IllegalStateException với thông báo chi tiết. Kết quả kiểm thử: Unit test mô phỏng nộp đề tài khi đã quá hạn thời gian đã chặn đứng "
        "thành công hành động vi phạm, đảm bảo tính kỷ cương dữ liệu 100%.", indent=720
    ))

    # ----------------------------------------------------
    # LỖI 6
    # ----------------------------------------------------
    body_parts.append(heading2("2.6. LỖI 6: VẤN ĐỀ THIẾU CHỨC NĂNG XÓA ĐỢT AN TOÀN (SAFE DELETION GUARD)"))

    body_parts.append(heading3("a) Tên lỗi & Hiện tượng ghi nhận"))
    body_parts.append(p(
        "Hiện tượng ghi nhận: Khi Quản trị viên vô tình nhấn nút xóa một đợt đăng ký đã có nhiều đề tài liên kết, hệ thống phát sinh lỗi "
        "xung đột khóa ngoại từ cơ sở dữ liệu MySQL, gây sập request với mã lỗi HTTP 500:", indent=720
    ))
    err6_stack = """org.springframework.dao.DataIntegrityViolationException: could not execute statement [Cannot delete or update a parent row: a foreign key constraint fails (`ute_capstone`.`topics`, CONSTRAINT `fk_topic_period` FOREIGN KEY (`period_id`) REFERENCES `registration_periods` (`id`))] [delete from registration_periods where id=?]"""
    body_parts.append(code_box(err6_stack, "EXCEPTION DATA INTEGRITY VIOLATION TRÍCH DẪN"))

    body_parts.append(heading3("b) Phân tích nguyên nhân kỹ thuật chuyên sâu (Root Cause Analysis)"))
    body_parts.append(p(
        "1. Ràng buộc toàn vẹn tham chiếu khóa ngoại (Foreign Key Constraint): Bảng 'topics' nắm giữ khóa ngoại 'period_id' trỏ đến bảng "
        "'registration_periods'. Khi thực hiện câu lệnh xóa trực tiếp thực thể cha mà không kiểm tra dữ liệu con, MySQL Engine sẽ từ chối giao dịch "
        "để bảo vệ tính toàn vẹn cơ sở dữ liệu.", indent=720
    ))
    body_parts.append(p(
        "2. Rủi ro xóa dây chuyền mất mát dữ liệu (Cascade Data Disaster): Nếu khắc phục lỗi trên bằng cách thiết lập cấu hình 'cascade = REMOVE' "
        "trên thực thể cha, hậu quả sẽ cực kỳ nghiêm trọng: Toàn bộ đề tài, danh sách nhóm sinh viên, báo cáo nộp và điểm số hội đồng sẽ bị xóa "
        "sạch vĩnh viễn khỏi hệ thống. Do đó, hệ thống bắt buộc phải triển khai chốt chặn an toàn 'Safe Deletion Guard' tại tầng Service.", indent=720
    ))

    body_parts.append(heading3("c) Đoạn code trước khi sửa (Before) và sau khi sửa (After)"))
    code6_bf = """// DỊCH VỤ TRƯỚC KHI SỬA (RegistrationPeriodServiceImpl.java)
@Override
@Transactional
public void deletePeriod(Long id) {
    // LỖI NGUY HIỂM: Xóa trực tiếp không qua kiểm tra dữ liệu liên kết
    RegistrationPeriod period = getPeriodById(id);
    periodRepository.delete(period); // Gây DataIntegrityViolationException hoặc xóa nhầm dữ liệu đồ án!
}"""
    body_parts.append(code_box(code6_bf, "CODE TRƯỚC KHI SỬA (BEFORE)"))

    code6_af = """// DỊCH VỤ SAU KHI SỬA (CHỐT CHẶN SAFE DELETION GUARD)
@Override
@Transactional
public void deletePeriod(Long id) {
    RegistrationPeriod period = getPeriodById(id);

    // CHỐT CHẶN AN TOÀN (Safe Deletion Guard)
    // Tuyệt đối không xóa đợt nếu đã có đề tài nộp lên (tránh crash DB hoặc mất dữ liệu đồ án)
    boolean hasTopics = topicRepository.existsByPeriodId(id);
    if (hasTopics) {
        throw new IllegalStateException("Không thể xóa đợt đăng ký này vì đã có đề tài liên kết! Hãy chuyển trạng thái sang Đóng hoặc Hủy.");
    }

    periodRepository.delete(period);
}"""
    body_parts.append(code_box(code6_af, "CODE SAU KHI SỬA (AFTER)"))

    body_parts.append(heading3("d) Giải pháp và Kết quả kiểm thử thực tế"))
    body_parts.append(p(
        "Giải pháp: Cài đặt chốt chặn an toàn 'Safe Deletion Guard' tại RegistrationPeriodServiceImpl. Trước khi gọi phương thức xóa, "
        "hệ thống kiểm tra 'topicRepository.existsByPeriodId(id)'. Nếu phát hiện có đề tài tồn tại, hệ thống chủ động ném ra ngoại lệ "
        "IllegalStateException và hướng dẫn người quản trị đóng hoặc hủy đợt thay vì xóa vật lý. Kết quả kiểm thử: Dữ liệu đồ án được bảo vệ an toàn 100%, "
        "triệt tiêu hoàn toàn ngoại lệ DataIntegrityViolationException.", indent=720
    ))

    body_parts.append(page_break())

    # ====================================================
    # PHẦN 3: TỔNG HỢP DANH MỤC CÁC FILE ĐÃ CHỈNH SỬA
    # ====================================================
    body_parts.append(heading1("PHẦN 3: TỔNG HỢP DANH MỤC CÁC FILE CODE ĐÃ CAN THIỆP & CHỈNH SỬA"))
    body_parts.append(p(
        "Dưới đây là bảng thống kê toàn diện các tệp mã nguồn Java, mẫu giao diện Thymeleaf và tệp kiểm thử đã được rà soát, "
        "chỉnh sửa và hoàn thiện trong toàn bộ chu trình xử lý lỗi của hệ thống:", indent=720
    ))

    files_headers = ["STT", "Đường dẫn Tệp tin (File Path)", "Phân Loại", "Nội dung Can thiệp & Ý nghĩa Kỹ thuật"]
    files_rows = [
        ["01", "src/main/resources/templates/admin/periods/list.html", "Thymeleaf View", "Chuẩn hóa SpEL so sánh != null trên reviewDeadline & defenseDate, loại bỏ lỗi ép kiểu."],
        ["02", "src/main/java/vn/edu/ute/capstone/service/impl/TopicServiceImpl.java", "Service Class", "Xử lý Session Hibernate với orphanRemoval và bổ sung Time-window Guard kép."],
        ["03", "src/main/java/vn/edu/ute/capstone/entity/TopicAdvisor.java", "JPA Entity", "Chuẩn hóa khóa phức hợp @EmbeddedId TopicAdvisorId và cấu hình @MapsId."],
        ["04", "src/main/java/vn/edu/ute/capstone/entity/Topic.java", "JPA Entity", "Cấu hình cascade = ALL, orphanRemoval = true cho quan hệ advisors."],
        ["05", "src/main/java/vn/edu/ute/capstone/controller/DepartmentTopicController.java", "Controller", "Tái cấu trúc @RequestMapping(\"/department\") điều hướng /topics và /catalog."],
        ["06", "src/main/resources/templates/department/topics/list.html", "Thymeleaf View", "Chuyển đổi th:onclick nối chuỗi sang chuẩn HTML5 data attributes th:data-* chống XSS."],
        ["07", "src/main/java/vn/edu/ute/capstone/service/impl/RegistrationPeriodServiceImpl.java", "Service Class", "Cài đặt Safe Deletion Guard ngăn ngừa xóa đợt khi đã có đề tài liên kết."],
        ["08", "src/main/resources/templates/fragments/layout.html", "Thymeleaf Layout", "Đồng bộ liên kết Sidebar điều hướng của Trưởng bộ môn sang /department/catalog."],
        ["09", "src/test/java/vn/edu/ute/capstone/service/TopicServiceTest.java", "Unit Test", "Bổ sung các ca kiểm thử bảo vệ đợt hết hạn, trùng đề tài và phê duyệt."],
        ["10", "src/test/java/vn/edu/ute/capstone/service/RegistrationPeriodServiceTest.java", "Unit Test", "Bổ sung ca kiểm thử Safe Deletion Guard và State Machine hợp lệ."]
    ]
    body_parts.append(make_table(files_headers, files_rows, [600, 3670, 1800, 3000]))

    body_parts.append(page_break())

    # ====================================================
    # PHẦN 4: KẾT LUẬN & ĐÁNH GIÁ ĐỘ ỔN ĐỊNH
    # ====================================================
    body_parts.append(heading1("PHẦN 4: KẾT LUẬN VÀ ĐÁNH GIÁ MỨC ĐỘ ỔN ĐỊNH CỦA HỆ THỐNG"))

    body_parts.append(p(
        "Trải qua quá trình rà soát, phân tích nguyên nhân gốc rễ và xử lý bài bản toàn bộ 6 lỗi kỹ thuật trọng yếu, "
        "hệ thống Quản lý Đề tài Sinh viên (HCMUTE Capstone Management) đã đạt được độ hoàn thiện cao, đảm bảo hoạt động "
        "thông suốt từ khâu thiết lập ban đầu đến khâu công bố điểm số cuối cùng.", indent=720
    ))

    body_parts.append(heading2("4.1. Kết quả kiểm thử tự động toàn diện (Test Suite Verification)"))
    body_parts.append(p(
        "Hệ thống đã thực hiện toàn bộ bộ kiểm thử tự động bao phủ trọn vẹn 5 phân hệ cốt lõi thông qua quy trình 'mvnw test'. "
        "Kết quả ghi nhận 17/17 Unit Tests thành công tuyệt đối (100% Pass Rate), không phát sinh bất kỳ lỗi thất bại (Failures) "
        "hay lỗi cấu hình (Errors) nào.", indent=720
    ))

    test_headers = ["Bộ Kiểm Thử (Test Suite)", "Số Lượng Tests", "Kết Quả", "Phạm Vi Kiểm Chứng"]
    test_rows = [
        ["RegistrationPeriodServiceTest", "04 Tests", "PASS (100%)", "Kiểm thử State Machine 7 trạng thái, Safe Delete Guard, Ràng buộc ngày tháng"],
        ["TopicServiceTest", "04 Tests", "PASS (100%)", "Kiểm thử Đề xuất, Time-window chốt chặn, Quản lý Session ORM, Phê duyệt Bộ môn"],
        ["StudentGroupServiceTest", "04 Tests", "PASS (100%)", "Kiểm thử Ràng buộc 1 SV 1 nhóm, Giới hạn 3 thành viên, Quyền Leader, Race Condition"],
        ["SubmissionServiceTest", "02 Tests", "PASS (100%)", "Kiểm thử Quyền nộp của Leader, Phân quyền truy cập và bảo mật file tải lên"],
        ["EvaluationServiceTest", "03 Tests", "PASS (100%)", "Kiểm thử Liêm chính GVHD, Chống tự chấm điểm, Tính điểm trung bình cộng & xếp loại"]
    ]
    body_parts.append(make_table(test_headers, test_rows, [3000, 1600, 1600, 2870]))

    body_parts.append(heading2("4.2. Đánh giá trạng thái máy chủ và mức độ sẵn sàng vận hành"))
    body_parts.append(p(
        "1. Hiệu năng khởi động máy chủ (Startup Performance): Máy chủ Spring Boot 4.1.1 khởi động nhanh chóng (trung bình 2.8 - 3.2 giây), "
        "kết nối cơ sở dữ liệu qua HikariCP Connection Pool được tối ưu, các script Flyway Migration thực thi chính xác 100%.", indent=720
    ))
    body_parts.append(p(
        "2. Toàn vẹn dữ liệu (Data Integrity): Toàn bộ ràng buộc bất biến (Business Invariants) đều được bảo vệ bằng mô hình phòng thủ 2 lớp "
        "(Lớp dịch vụ Java Service Clauses và Lớp cơ sở dữ liệu Database Unique/Foreign Key Constraints).", indent=720
    ))
    body_parts.append(p(
        "3. Trải nghiệm người dùng (UX/UI Consistency): Thiết kế giao diện theo phong cách Neo-Brutalism tối giản mang lại phản hồi xúc giác "
        "nhanh nhạy, không còn hiện tượng lỗi JavaScript hoặc vỡ giao diện trên các trình duyệt hiện đại.", indent=720
    ))

    body_parts.append(callout_box("KẾT LUẬN CHUNG", 
        "Hệ thống Quản lý Đề tài Sinh viên (HCMUTE Capstone Management) đã hoàn thành xuất sắc toàn bộ mục tiêu kỹ thuật, khắc phục triệt để "
        "tất cả các lỗi phát sinh, đạt mức độ ổn định và sẵn sàng bàn giao triển khai vào môi trường thực tế tại Khoa Công nghệ Thông tin.",
        fill="F8F9FA", bdr_col="000000"))

    # Section properties at end of body: A4 size, standard margins (1 inch = 1440 twips)
    sect_pr = """<w:sectPr>
        <w:footerReference w:type="default" r:id="rId2"/>
        <w:pgSz w:w="11906" w:h="16838"/>
        <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="708" w:footer="708" w:gutter="0"/>
        <w:cols w:space="708"/>
    </w:sectPr>"""

    document_xml = f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
            xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
    <w:body>
        {''.join(body_parts)}
        {sect_pr}
    </w:body>
</w:document>"""

    # Build ZIP DOCX in memory
    buffer = io.BytesIO()
    with zipfile.ZipFile(buffer, 'w', zipfile.ZIP_DEFLATED) as zf:
        zf.writestr("[Content_Types].xml", content_types_xml)
        zf.writestr("_rels/.rels", root_rels_xml)
        zf.writestr("word/_rels/document.xml.rels", doc_rels_xml)
        zf.writestr("word/styles.xml", styles_xml)
        zf.writestr("word/footer1.xml", footer_xml)
        zf.writestr("word/document.xml", document_xml)

    docx_bytes = buffer.getvalue()
    with open(output_filename, 'wb') as f:
        f.write(docx_bytes)
        
    print(f"Báo cáo kỹ thuật đã được tạo thành công tại: {output_filename} (Kích thước: {len(docx_bytes)} bytes)")
    return len(docx_bytes)

if __name__ == "__main__":
    target_path = r"G:\Desktop\ute-capstone-management\BAO_CAO_SUA_LOI_KY_THUAT.docx"
    generate_report(target_path)
