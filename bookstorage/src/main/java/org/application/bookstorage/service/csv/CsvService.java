package org.application.bookstorage.service.csv;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.*;
import org.application.bookstorage.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CsvService {

    private static final Logger logger = LoggerFactory.getLogger(CsvService.class);

    private final AuthorRepository authorRepository;
    private final PublishingCompanyRepository publishingCompanyRepository;
    private final BookRepository bookRepository;
    private final AuthorshipRepository authorshipRepository;
    private final StylesRepository stylesRepository;
    private final BookStylesRepository bookStylesRepository;
    private final JdbcTemplate jdbcTemplate; // Для выполнения SQL-запроса очистки БД

    // Путь к каталогу для экспорта/импорта CSV файлов задаётся через application.properties
    @Value("${csv.export.dir}")
    private String exportDir;

    /**
     * Экспортирует все данные из базы данных в CSV файлы.
     */
    @Transactional
    public void exportData() throws IOException {
        // Создание каталога для экспорта, если он не существует
        Path exportPath = Paths.get(exportDir);
        if (!Files.exists(exportPath)) {
            Files.createDirectories(exportPath);
            logger.info("Создана директория для экспорта: {}", exportPath.toAbsolutePath());
        }

        exportAuthors();
        exportPublishingCompanies();
        exportStyles();
        exportBooks();
        exportAuthorships();
        exportBookStyles();
        logger.info("Экспорт данных завершён.");
    }

    /**
     * Импортирует все данные из CSV файлов в базу данных.
     * Перед импортом очищается база данных с помощью метода clearDatabase().
     */
    @Transactional
    public void importData() throws IOException, CsvValidationException {
        // Очистка БД перед импортом
        clearDatabase();

        // Порядок импорта для соблюдения зависимостей
        importPublishingCompanies();
        importAuthors();
        importStyles();
        importBooks();
        importAuthorships();
        importBookStyles();
        logger.info("Импорт данных завершён.");
    }

    /**
     * Очищает все таблицы схемы 'public' (TRUNCATE с RESTART IDENTITY CASCADE).
     */
    private void clearDatabase() {
        String sql = "DO $$\n" +
                "DECLARE\n" +
                "    r RECORD;\n" +
                "BEGIN\n" +
                "    FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP\n" +
                "        EXECUTE 'TRUNCATE TABLE ' || quote_ident(r.tablename) || ' RESTART IDENTITY CASCADE';\n" +
                "    END LOOP;\n" +
                "END;\n" +
                "$$;";
        jdbcTemplate.execute(sql);
        logger.info("База данных очищена перед импортом данных.");
    }

    // =================================================================
    //                            ЭКСПОРТ
    // =================================================================

    private void exportAuthors() throws IOException {
        String[] header = {"id", "fio", "birth_date", "country", "nickname"};
        List<Author> authors = authorRepository.findAll();

        try (FileOutputStream fos = new FileOutputStream(exportDir + "/authors.csv");
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw)) {

            // Запись BOM
            bw.write('\uFEFF');
            bw.flush();

            try (CSVWriter writer = new CSVWriter(
                    bw,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END)) {

                writer.writeNext(header);
                for (Author author : authors) {
                    String[] data = {
                            author.getId() != null ? String.valueOf(author.getId()) : "",
                            author.getFio(),
                            author.getBirthDate(),
                            author.getCountry(),
                            author.getNickname()
                    };
                    writer.writeNext(data);
                }
            }
            logger.info("Экспортировано {} авторов (authors.csv).", authors.size());
        }
    }

    private void exportPublishingCompanies() throws IOException {
        String[] header = {"name", "establishment_year", "contact_info", "city"};
        List<PublishingCompany> companies = publishingCompanyRepository.findAll();

        try (FileOutputStream fos = new FileOutputStream(exportDir + "/publishing_companies.csv");
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw)) {

            bw.write('\uFEFF');
            bw.flush();

            try (CSVWriter writer = new CSVWriter(
                    bw,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END)) {

                writer.writeNext(header);
                for (PublishingCompany company : companies) {
                    String estYear = (company.getEstablishmentYear() == null)
                            ? ""
                            : String.valueOf(company.getEstablishmentYear());
                    String[] data = {
                            company.getName(),
                            estYear,
                            company.getContactInfo(),
                            company.getCity()
                    };
                    writer.writeNext(data);
                }
            }
            logger.info("Экспортировано {} издательств (publishing_companies.csv).", companies.size());
        }
    }

    private void exportStyles() throws IOException {
        String[] header = {"id", "name"};
        List<Styles> styles = stylesRepository.findAll();

        try (FileOutputStream fos = new FileOutputStream(exportDir + "/styles.csv");
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw)) {

            bw.write('\uFEFF');
            bw.flush();

            try (CSVWriter writer = new CSVWriter(
                    bw,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END)) {

                writer.writeNext(header);
                for (Styles style : styles) {
                    String[] data = {
                            style.getId() != null ? String.valueOf(style.getId()) : "",
                            style.getName()
                    };
                    writer.writeNext(data);
                }
            }
            logger.info("Экспортировано {} стилей (styles.csv).", styles.size());
        }
    }

    private void exportBooks() throws IOException {
        String[] header = {
                "isbn", "name", "publication_year", "age_limit",
                "publishing_company", "page_count", "language", "cost", "count_of_books"
        };
        List<Book> books = bookRepository.findAll();

        try (FileOutputStream fos = new FileOutputStream(exportDir + "/books.csv");
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw)) {

            bw.write('\uFEFF');
            bw.flush();

            try (CSVWriter writer = new CSVWriter(
                    bw,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END)) {

                writer.writeNext(header);
                for (Book book : books) {
                    String pubYear = (book.getPublicationYear() == null)
                            ? ""
                            : String.valueOf(book.getPublicationYear());
                    String pubCompany = (book.getPublishingCompany() != null)
                            ? book.getPublishingCompany().getName()
                            : "";
                    String[] data = {
                            book.getIsbn(),
                            book.getName(),
                            pubYear,
                            String.valueOf(book.getAgeLimit()),
                            pubCompany,
                            String.valueOf(book.getPageCount()),
                            book.getLanguage(),
                            String.valueOf(book.getCost()),
                            String.valueOf(book.getCountOfBooks())
                    };
                    writer.writeNext(data);
                }
            }
            logger.info("Экспортировано {} книг (books.csv).", books.size());
        }
    }

    private void exportAuthorships() throws IOException {
        String[] header = {"book_isbn", "author_id"};
        List<Authorship> authorships = authorshipRepository.findAll();

        try (FileOutputStream fos = new FileOutputStream(exportDir + "/authorships.csv");
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw)) {

            bw.write('\uFEFF');
            bw.flush();

            try (CSVWriter writer = new CSVWriter(
                    bw,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END)) {

                writer.writeNext(header);
                for (Authorship authorship : authorships) {
                    String[] data = {
                            authorship.getBook().getIsbn(),
                            (authorship.getAuthor().getId() != null ? String.valueOf(authorship.getAuthor().getId()) : "")
                    };
                    writer.writeNext(data);
                }
            }
            logger.info("Экспортировано {} авторств (authorships.csv).", authorships.size());
        }
    }

    private void exportBookStyles() throws IOException {
        String[] header = {"book_isbn", "style_id"};
        List<BookStyles> bookStyles = bookStylesRepository.findAll();

        try (FileOutputStream fos = new FileOutputStream(exportDir + "/book_styles.csv");
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw)) {

            bw.write('\uFEFF');
            bw.flush();

            try (CSVWriter writer = new CSVWriter(
                    bw,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END)) {

                writer.writeNext(header);
                for (BookStyles bs : bookStyles) {
                    String[] data = {
                            bs.getBook().getIsbn(),
                            (bs.getStyleEntity().getId() != null ? String.valueOf(bs.getStyleEntity().getId()) : "")
                    };
                    writer.writeNext(data);
                }
            }
            logger.info("Экспортировано {} связей книг со стилями (book_styles.csv).", bookStyles.size());
        }
    }

    // =================================================================
    //                            ИМПОРТ
    // =================================================================

    /**
     * Метод для попытки пропуска BOM, если он есть.
     * @param in входной поток
     */
    private void skipBomIfPresent(BufferedInputStream in) throws IOException {
        in.mark(3);  // Запоминаем первые 3 байта
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        // UTF-8 BOM: 0xEF,0xBB,0xBF
        if (!(b1 == 0xEF && b2 == 0xBB && b3 == 0xBF)) {
            // Если BOM не распознан, возвращаемся обратно
            in.reset();
        }
    }

    /**
     * Создаёт CSVReader, который принудительно игнорирует кавычки и парсит поля по запятой.
     */
    private CSVReader buildCsvReader(InputStreamReader isr) {
        // Настраиваем парсер, чтобы не ожидать кавычки:
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(',')
                .withIgnoreQuotations(true)
                .build();

        return new CSVReaderBuilder(isr)
                .withCSVParser(parser)
                .build();
    }

    private void importAuthors() throws IOException, CsvValidationException {
        Path filePath = Paths.get(exportDir + "/authors.csv");
        if (!Files.exists(filePath)) {
            logger.warn("authors.csv не найден, пропуск импорта авторов.");
            return;
        }

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             BufferedInputStream bis = new BufferedInputStream(fis);
             InputStreamReader isr = new InputStreamReader(bis, StandardCharsets.UTF_8)) {

            skipBomIfPresent(bis);

            try (CSVReader reader = buildCsvReader(isr)) {
                // Пропускаем заголовок
                reader.readNext();

                int imported = 0;
                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) {
                    if (nextLine.length < 5) {
                        logger.warn("Некорректная строка в authors.csv: {}", Arrays.toString(nextLine));
                        continue;
                    }
                    try {
                        String idStr = nextLine[0];
                        Integer id = null;
                        if (idStr != null && !idStr.trim().isEmpty()) {
                            id = Integer.valueOf(idStr);
                        }
                        String fio = nextLine[1];
                        String birthDate = nextLine[2];
                        String country = nextLine[3];
                        String nickname = nextLine[4];

                        if (id != null) {
                            Optional<Author> existingAuthor = authorRepository.findById(id);
                            if (existingAuthor.isPresent()) {
                                Author author = existingAuthor.get();
                                author.setFio(fio);
                                author.setBirthDate(birthDate);
                                author.setCountry(country);
                                author.setNickname(nickname);
                                authorRepository.save(author);
                                imported++;
                                continue;
                            }
                        }
                        // Создание нового
                        Author author = new Author();
                        author.setFio(fio);
                        author.setBirthDate(birthDate);
                        author.setCountry(country);
                        author.setNickname(nickname);
                        authorRepository.save(author);
                        imported++;
                    } catch (NumberFormatException e) {
                        logger.error("Ошибка парсинга числа в authors.csv: {}", Arrays.toString(nextLine), e);
                    } catch (Exception e) {
                        logger.error("Не удалось импортировать автора из строки: {}", Arrays.toString(nextLine), e);
                    }
                }
                logger.info("Импортировано {} авторов.", imported);
            }
        }
    }

    private void importPublishingCompanies() throws IOException, CsvValidationException {
        Path filePath = Paths.get(exportDir + "/publishing_companies.csv");
        if (!Files.exists(filePath)) {
            logger.warn("publishing_companies.csv не найден, пропуск импорта издательств.");
            return;
        }

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             BufferedInputStream bis = new BufferedInputStream(fis);
             InputStreamReader isr = new InputStreamReader(bis, StandardCharsets.UTF_8)) {

            skipBomIfPresent(bis);

            try (CSVReader reader = buildCsvReader(isr)) {
                // Пропускаем заголовок
                reader.readNext();

                int imported = 0;
                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) {
                    if (nextLine.length < 4) {
                        logger.warn("Некорректная строка в publishing_companies.csv: {}", Arrays.toString(nextLine));
                        continue;
                    }
                    try {
                        String name = nextLine[0];
                        String estYearStr = nextLine[1];
                        String contactInfo = nextLine[2];
                        String city = nextLine[3];

                        LocalDate establishmentYear = null;
                        if (estYearStr != null && !estYearStr.trim().isEmpty()) {
                            if (estYearStr.length() == 4) {
                                estYearStr += "-01-01";
                            }
                            establishmentYear = LocalDate.parse(estYearStr);
                        }

                        Optional<PublishingCompany> existingCompany = publishingCompanyRepository.findById(name);
                        if (existingCompany.isPresent()) {
                            // Обновление
                            PublishingCompany company = existingCompany.get();
                            company.setEstablishmentYear(establishmentYear);
                            company.setContactInfo(contactInfo);
                            company.setCity(city);
                            publishingCompanyRepository.save(company);
                        } else {
                            // Создание нового
                            PublishingCompany company = new PublishingCompany();
                            company.setName(name);
                            company.setEstablishmentYear(establishmentYear);
                            company.setContactInfo(contactInfo);
                            company.setCity(city);
                            publishingCompanyRepository.save(company);
                        }
                        imported++;
                    } catch (NumberFormatException e) {
                        logger.error("Ошибка парсинга года в publishing_companies.csv: {}", Arrays.toString(nextLine), e);
                    } catch (Exception e) {
                        logger.error("Не удалось импортировать издательство из строки: {}", Arrays.toString(nextLine), e);
                    }
                }
                logger.info("Импортировано {} издательств.", imported);
            }
        }
    }

    private void importStyles() throws IOException, CsvValidationException {
        Path filePath = Paths.get(exportDir + "/styles.csv");
        if (!Files.exists(filePath)) {
            logger.warn("styles.csv не найден, пропуск импорта стилей.");
            return;
        }

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             BufferedInputStream bis = new BufferedInputStream(fis);
             InputStreamReader isr = new InputStreamReader(bis, StandardCharsets.UTF_8)) {

            skipBomIfPresent(bis);

            try (CSVReader reader = buildCsvReader(isr)) {
                // Пропускаем заголовок
                reader.readNext();

                int imported = 0;
                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) {
                    if (nextLine.length < 2) {
                        logger.warn("Некорректная строка в styles.csv: {}", Arrays.toString(nextLine));
                        continue;
                    }
                    try {
                        String idStr = nextLine[0];
                        Long id = null;
                        if (idStr != null && !idStr.trim().isEmpty()) {
                            id = Long.valueOf(idStr);
                        }
                        String name = nextLine[1];

                        if (id != null) {
                            Optional<Styles> existingStyle = stylesRepository.findById(id);
                            if (existingStyle.isPresent()) {
                                Styles style = existingStyle.get();
                                style.setName(name);
                                stylesRepository.save(style);
                                imported++;
                                continue;
                            }
                        }
                        // Создание нового
                        Styles style = new Styles();
                        style.setName(name);
                        stylesRepository.save(style);
                        imported++;
                    } catch (NumberFormatException e) {
                        logger.error("Ошибка парсинга числа в styles.csv: {}", Arrays.toString(nextLine), e);
                    } catch (Exception e) {
                        logger.error("Не удалось импортировать стиль из строки: {}", Arrays.toString(nextLine), e);
                    }
                }
                logger.info("Импортировано {} стилей.", imported);
            }
        }
    }

    private void importBooks() throws IOException, CsvValidationException {
        Path filePath = Paths.get(exportDir + "/books.csv");
        if (!Files.exists(filePath)) {
            logger.warn("books.csv не найден, пропуск импорта книг.");
            return;
        }

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             BufferedInputStream bis = new BufferedInputStream(fis);
             InputStreamReader isr = new InputStreamReader(bis, StandardCharsets.UTF_8)) {

            skipBomIfPresent(bis);

            try (CSVReader reader = buildCsvReader(isr)) {
                // Пропускаем заголовок
                reader.readNext();

                int imported = 0;
                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) {
                    if (nextLine.length < 9) {
                        logger.warn("Некорректная строка в books.csv: {}", Arrays.toString(nextLine));
                        continue;
                    }
                    try {
                        String isbn = nextLine[0];
                        String name = nextLine[1];
                        String pubYearStr = nextLine[2];
                        float ageLimit = Float.parseFloat(nextLine[3]);
                        String publishingCompanyName = nextLine[4];
                        int pageCount = Integer.parseInt(nextLine[5]);
                        String language = nextLine[6];
                        float cost = Float.parseFloat(nextLine[7]);
                        int countOfBooks = Integer.parseInt(nextLine[8]);

                        LocalDate publicationYear = null;
                        if (pubYearStr != null && !pubYearStr.trim().isEmpty()) {
                            publicationYear = LocalDate.parse(pubYearStr);
                        }

                        PublishingCompany publishingCompany = null;
                        if (publishingCompanyName != null && !publishingCompanyName.isEmpty()) {
                            publishingCompany = publishingCompanyRepository.findById(publishingCompanyName)
                                    .orElseThrow(() -> new RuntimeException("Издательство не найдено: " + publishingCompanyName));
                        }

                        Optional<Book> existingBook = bookRepository.findById(isbn);
                        if (existingBook.isPresent()) {
                            // Обновление
                            Book book = existingBook.get();
                            book.setName(name);
                            book.setPublicationYear(publicationYear);
                            book.setAgeLimit(ageLimit);
                            book.setPublishingCompany(publishingCompany);
                            book.setPageCount(pageCount);
                            book.setLanguage(language);
                            book.setCost(new java.math.BigDecimal(cost));
                            book.setCountOfBooks(countOfBooks);
                            bookRepository.save(book);
                        } else {
                            // Создание нового
                            Book book = new Book();
                            book.setIsbn(isbn);
                            book.setName(name);
                            book.setPublicationYear(publicationYear);
                            book.setAgeLimit(ageLimit);
                            book.setPublishingCompany(publishingCompany);
                            book.setPageCount(pageCount);
                            book.setLanguage(language);
                            book.setCost(new java.math.BigDecimal(cost));
                            book.setCountOfBooks(countOfBooks);
                            bookRepository.save(book);
                        }
                        imported++;
                    } catch (NumberFormatException e) {
                        logger.error("Ошибка парсинга числа в books.csv: {}", Arrays.toString(nextLine), e);
                    } catch (Exception e) {
                        logger.error("Не удалось импортировать книгу из строки: {}", Arrays.toString(nextLine), e);
                    }
                }
                logger.info("Импортировано {} книг.", imported);
            }
        }
    }

    private void importAuthorships() throws IOException, CsvValidationException {
        Path filePath = Paths.get(exportDir + "/authorships.csv");
        if (!Files.exists(filePath)) {
            logger.warn("authorships.csv не найден, пропуск импорта авторств.");
            return;
        }

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             BufferedInputStream bis = new BufferedInputStream(fis);
             InputStreamReader isr = new InputStreamReader(bis, StandardCharsets.UTF_8)) {

            skipBomIfPresent(bis);

            try (CSVReader reader = buildCsvReader(isr)) {
                // Пропускаем заголовок
                reader.readNext();

                int imported = 0;
                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) {
                    if (nextLine.length < 2) {
                        logger.warn("Некорректная строка в authorships.csv: {}", Arrays.toString(nextLine));
                        continue;
                    }
                    try {
                        String bookIsbn = nextLine[0];
                        String authorIdStr = nextLine[1];
                        Integer authorId = null;
                        if (authorIdStr != null && !authorIdStr.trim().isEmpty()) {
                            authorId = Integer.valueOf(authorIdStr);
                        }

                        if (bookIsbn == null || bookIsbn.trim().isEmpty() || authorId == null) {
                            logger.warn("Некорректные данные для авторства: {}", Arrays.toString(nextLine));
                            continue;
                        }

                        Optional<Book> bookOpt = bookRepository.findById(bookIsbn);
                        Optional<Author> authorOpt = authorRepository.findById(authorId);

                        if (bookOpt.isPresent() && authorOpt.isPresent()) {
                            Book book = bookOpt.get();
                            Author author = authorOpt.get();
                            AuthorshipId id = new AuthorshipId(bookIsbn, authorId);
                            Optional<Authorship> existingAuthorship = authorshipRepository.findById(id);
                            if (!existingAuthorship.isPresent()) {
                                Authorship authorship = new Authorship();
                                authorship.setId(id);
                                authorship.setBook(book);
                                authorship.setAuthor(author);
                                authorshipRepository.save(authorship);
                                imported++;
                            } else {
                                logger.debug("Авторство уже существует: ISBN={}, AuthorID={}", bookIsbn, authorId);
                            }
                        } else {
                            logger.warn("Книга или автор не найдены для авторства: ISBN={}, AuthorID={}", bookIsbn, authorId);
                        }
                    } catch (NumberFormatException e) {
                        logger.error("Ошибка парсинга числа в authorships.csv: {}", Arrays.toString(nextLine), e);
                    } catch (Exception e) {
                        logger.error("Не удалось импортировать авторство из строки: {}", Arrays.toString(nextLine), e);
                    }
                }
                logger.info("Импортировано {} авторств.", imported);
            }
        }
    }

    private void importBookStyles() throws IOException, CsvValidationException {
        Path filePath = Paths.get(exportDir + "/book_styles.csv");
        if (!Files.exists(filePath)) {
            logger.warn("book_styles.csv не найден, пропуск импорта стилей книг.");
            return;
        }

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             BufferedInputStream bis = new BufferedInputStream(fis);
             InputStreamReader isr = new InputStreamReader(bis, StandardCharsets.UTF_8)) {

            skipBomIfPresent(bis);

            try (CSVReader reader = buildCsvReader(isr)) {
                // Пропускаем строку заголовка
                reader.readNext();

                int imported = 0;
                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) {
                    if (nextLine.length < 2) {
                        logger.warn("Некорректная строка в book_styles.csv: {}", Arrays.toString(nextLine));
                        continue;
                    }
                    try {
                        String bookIsbn = nextLine[0];
                        String styleIdStr = nextLine[1];
                        Long styleId = null;
                        if (styleIdStr != null && !styleIdStr.trim().isEmpty()) {
                            styleId = Long.valueOf(styleIdStr);
                        }

                        if (bookIsbn == null || bookIsbn.trim().isEmpty() || styleId == null) {
                            logger.warn("Некорректные данные для связи книги со стилем: {}", Arrays.toString(nextLine));
                            continue;
                        }

                        Optional<Book> bookOpt = bookRepository.findById(bookIsbn);
                        Optional<Styles> styleOpt = stylesRepository.findById(styleId);

                        if (bookOpt.isPresent() && styleOpt.isPresent()) {
                            Book book = bookOpt.get();
                            Styles style = styleOpt.get();

                            BookStylesId id = new BookStylesId(bookIsbn, styleId);
                            Optional<BookStyles> existingBookStyle = bookStylesRepository.findById(id);

                            if (!existingBookStyle.isPresent()) {
                                BookStyles bookStyles = new BookStyles();
                                bookStyles.setId(id);
                                bookStyles.setBook(book);
                                bookStyles.setStyleEntity(style);
                                bookStylesRepository.save(bookStyles);
                                imported++;
                            } else {
                                logger.debug("Связь книги со стилем уже существует: ISBN={}, StyleID={}", bookIsbn, styleId);
                            }
                        } else {
                            logger.warn("Книга или стиль не найдены для связи: ISBN={}, StyleID={}", bookIsbn, styleId);
                        }
                    } catch (NumberFormatException e) {
                        logger.error("Ошибка парсинга числа в book_styles.csv: {}", Arrays.toString(nextLine), e);
                    } catch (Exception e) {
                        logger.error("Не удалось импортировать связь книги со стилем из строки: {}", Arrays.toString(nextLine), e);
                    }
                }
                logger.info("Импортировано {} связей книг со стилями.", imported);
            }
        }
    }
}
