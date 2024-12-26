package org.application.bookstorage.service.csv;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.*;
import org.application.bookstorage.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
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

    // Каталог для сохранения и чтения CSV файлов
    private final String EXPORT_DIR = "csv_exports";

    /**
     * Экспортирует все данные из базы данных в CSV файлы.
     */
    @Transactional
    public void exportData() throws IOException {
        // Создание каталога для экспорта, если он не существует
        Path exportPath = Paths.get(EXPORT_DIR);
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
     */
    @Transactional
    public void importData() throws IOException, CsvValidationException {
        // Порядок импорта для соблюдения зависимостей
        importPublishingCompanies();
        importAuthors();
        importStyles();
        importBooks();
        importAuthorships();
        importBookStyles();
        logger.info("Импорт данных завершён.");
    }

    // Методы экспорта для каждой сущности

    private void exportAuthors() throws IOException {
        String[] header = {"id", "fio", "birth_date", "country", "nickname"};
        List<Author> authors = authorRepository.findAll();
        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(
                        new FileOutputStream(EXPORT_DIR + "/authors.csv"),
                        StandardCharsets.UTF_8
                ),
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END
        )) {
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
            logger.info("Экспортировано {} авторов.", authors.size());
        }
    }

    private void exportPublishingCompanies() throws IOException {
        String[] header = {"name", "establishment_year", "contact_info", "city"};
        List<PublishingCompany> companies = publishingCompanyRepository.findAll();
        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(
                        new FileOutputStream(EXPORT_DIR + "/publishing_companies.csv"),
                        StandardCharsets.UTF_8
                ),
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END
        )) {
            writer.writeNext(header);
            for (PublishingCompany company : companies) {
                String[] data = {
                        company.getName(),
                        String.valueOf(company.getEstablishmentYear()),
                        company.getContactInfo(),
                        company.getCity()
                };
                writer.writeNext(data);
            }
            logger.info("Экспортировано {} издательств.", companies.size());
        }
    }

    private void exportStyles() throws IOException {
        String[] header = {"id", "name"};
        List<Styles> styles = stylesRepository.findAll();
        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(
                        new FileOutputStream(EXPORT_DIR + "/styles.csv"),
                        StandardCharsets.UTF_8
                ),
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END
        )) {
            writer.writeNext(header);
            for (Styles style : styles) {
                String[] data = {
                        style.getId() != null ? String.valueOf(style.getId()) : "",
                        style.getName()
                };
                writer.writeNext(data);
            }
            logger.info("Экспортировано {} стилей.", styles.size());
        }
    }

    private void exportBooks() throws IOException {
        String[] header = {"isbn", "name", "publication_year", "age_limit", "publishing_company", "page_count", "language", "cost", "count_of_books"};
        List<Book> books = bookRepository.findAll();
        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(
                        new FileOutputStream(EXPORT_DIR + "/books.csv"),
                        StandardCharsets.UTF_8
                ),
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END
        )) {
            writer.writeNext(header);
            for (Book book : books) {
                String[] data = {
                        book.getIsbn(),
                        book.getName(),
                        book.getPublicationYear(),
                        String.valueOf(book.getAgeLimit()),
                        book.getPublishingCompany() != null ? book.getPublishingCompany().getName() : "",
                        String.valueOf(book.getPageCount()),
                        book.getLanguage(),
                        String.valueOf(book.getCost()),
                        String.valueOf(book.getCountOfBooks())
                };
                writer.writeNext(data);
            }
            logger.info("Экспортировано {} книг.", books.size());
        }
    }

    private void exportAuthorships() throws IOException {
        String[] header = {"book_isbn", "author_id"};
        List<Authorship> authorships = authorshipRepository.findAll();
        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(
                        new FileOutputStream(EXPORT_DIR + "/authorships.csv"),
                        StandardCharsets.UTF_8
                ),
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END
        )) {
            writer.writeNext(header);
            for (Authorship authorship : authorships) {
                String[] data = {
                        authorship.getBook().getIsbn(),
                        authorship.getAuthor().getId() != null ? String.valueOf(authorship.getAuthor().getId()) : ""
                };
                writer.writeNext(data);
            }
            logger.info("Экспортировано {} авторств.", authorships.size());
        }
    }

    private void exportBookStyles() throws IOException {
        String[] header = {"book_isbn", "style_id"};
        List<BookStyles> bookStyles = bookStylesRepository.findAll();
        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(
                        new FileOutputStream(EXPORT_DIR + "/book_styles.csv"),
                        StandardCharsets.UTF_8
                ),
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END
        )) {
            writer.writeNext(header);
            for (BookStyles bs : bookStyles) {
                String[] data = {
                        bs.getBook().getIsbn(),
                        bs.getStyleEntity().getId() != null ? String.valueOf(bs.getStyleEntity().getId()) : ""
                };
                writer.writeNext(data);
            }
            logger.info("Экспортировано {} связей книг со стилями.", bookStyles.size());
        }
    }

    // Методы импорта для каждой сущности

    private void importAuthors() throws IOException, CsvValidationException {
        Path filePath = Paths.get(EXPORT_DIR + "/authors.csv");
        if (!Files.exists(filePath)) {
            logger.warn("authors.csv не найден, пропуск импорта авторов.");
            return;
        }

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(
                        new FileInputStream(filePath.toFile()),
                        StandardCharsets.UTF_8
                )
        )) {
            String[] nextLine;
            reader.readNext(); // Пропуск заголовка
            int imported = 0;
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
                        // Попытка найти существующего автора по ID
                        Optional<Author> existingAuthor = authorRepository.findById(id);
                        if (existingAuthor.isPresent()) {
                            // Обновление существующего автора
                            Author author = existingAuthor.get();
                            author.setFio(fio);
                            author.setBirthDate(birthDate);
                            author.setCountry(country);
                            author.setNickname(nickname);
                            authorRepository.save(author);
                            logger.debug("Обновлен автор с ID {}", id);
                            imported++;
                            continue;
                        }
                    }

                    // Создание нового автора без установки ID
                    Author author = new Author();
                    author.setFio(fio);
                    author.setBirthDate(birthDate);
                    author.setCountry(country);
                    author.setNickname(nickname);
                    authorRepository.save(author);
                    logger.debug("Создан новый автор с ID {}", author.getId());
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

    private void importPublishingCompanies() throws IOException, CsvValidationException {
        Path filePath = Paths.get(EXPORT_DIR + "/publishing_companies.csv");
        if (!Files.exists(filePath)) {
            logger.warn("publishing_companies.csv не найден, пропуск импорта издательств.");
            return;
        }

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(
                        new FileInputStream(filePath.toFile()),
                        StandardCharsets.UTF_8
                )
        )) {
            String[] nextLine;
            reader.readNext(); // Пропуск заголовка
            int imported = 0;
            while ((nextLine = reader.readNext()) != null) {
                if (nextLine.length < 4) {
                    logger.warn("Некорректная строка в publishing_companies.csv: {}", Arrays.toString(nextLine));
                    continue;
                }
                try {
                    String name = nextLine[0];
                    int establishmentYear = Integer.parseInt(nextLine[1]);
                    String contactInfo = nextLine[2];
                    String city = nextLine[3];

                    Optional<PublishingCompany> existingCompany = publishingCompanyRepository.findById(name);
                    if (existingCompany.isPresent()) {
                        // Обновление существующего издательства
                        PublishingCompany company = existingCompany.get();
                        company.setEstablishmentYear(establishmentYear);
                        company.setContactInfo(contactInfo);
                        company.setCity(city);
                        publishingCompanyRepository.save(company);
                        logger.debug("Обновлено издательство: {}", name);
                    } else {
                        // Создание нового издательства
                        PublishingCompany company = new PublishingCompany();
                        company.setName(name);
                        company.setEstablishmentYear(establishmentYear);
                        company.setContactInfo(contactInfo);
                        company.setCity(city);
                        publishingCompanyRepository.save(company);
                        logger.debug("Создано новое издательство: {}", name);
                    }
                    imported++;
                } catch (NumberFormatException e) {
                    logger.error("Ошибка парсинга числа в publishing_companies.csv: {}", Arrays.toString(nextLine), e);
                } catch (Exception e) {
                    logger.error("Не удалось импортировать издательство из строки: {}", Arrays.toString(nextLine), e);
                }
            }
            logger.info("Импортировано {} издательств.", imported);
        }
    }

    private void importStyles() throws IOException, CsvValidationException {
        Path filePath = Paths.get(EXPORT_DIR + "/styles.csv");
        if (!Files.exists(filePath)) {
            logger.warn("styles.csv не найден, пропуск импорта стилей.");
            return;
        }

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(
                        new FileInputStream(filePath.toFile()),
                        StandardCharsets.UTF_8
                )
        )) {
            String[] nextLine;
            reader.readNext(); // Пропуск заголовка
            int imported = 0;
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
                            // Обновление существующего стиля
                            Styles style = existingStyle.get();
                            style.setName(name);
                            stylesRepository.save(style);
                            logger.debug("Обновлён стиль с ID {}", id);
                            imported++;
                            continue;
                        }
                    }

                    // Создание нового стиля без установки ID
                    Styles style = new Styles();
                    style.setName(name);
                    stylesRepository.save(style);
                    logger.debug("Создан новый стиль с ID {}", style.getId());
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

    private void importBooks() throws IOException, CsvValidationException {
        Path filePath = Paths.get(EXPORT_DIR + "/books.csv");
        if (!Files.exists(filePath)) {
            logger.warn("books.csv не найден, пропуск импорта книг.");
            return;
        }

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(
                        new FileInputStream(filePath.toFile()),
                        StandardCharsets.UTF_8
                )
        )) {
            String[] nextLine;
            reader.readNext(); // Пропуск заголовка
            int imported = 0;
            while ((nextLine = reader.readNext()) != null) {
                if (nextLine.length < 9) {
                    logger.warn("Некорректная строка в books.csv: {}", Arrays.toString(nextLine));
                    continue;
                }
                try {
                    String isbn = nextLine[0];
                    String name = nextLine[1];
                    String publicationYear = nextLine[2];
                    float ageLimit = Float.parseFloat(nextLine[3]);
                    String publishingCompanyName = nextLine[4];
                    int pageCount = Integer.parseInt(nextLine[5]);
                    String language = nextLine[6];
                    float cost = Float.parseFloat(nextLine[7]);
                    int countOfBooks = Integer.parseInt(nextLine[8]);

                    // Получение издательства
                    PublishingCompany publishingCompany = null;
                    if (publishingCompanyName != null && !publishingCompanyName.isEmpty()) {
                        publishingCompany = publishingCompanyRepository.findById(publishingCompanyName)
                                .orElseThrow(() -> new RuntimeException("Издательство не найдено: " + publishingCompanyName));
                    }

                    Optional<Book> existingBook = bookRepository.findById(isbn);
                    if (existingBook.isPresent()) {
                        // Обновление существующей книги
                        Book book = existingBook.get();
                        book.setName(name);
                        book.setPublicationYear(publicationYear);
                        book.setAgeLimit(ageLimit);
                        book.setPublishingCompany(publishingCompany);
                        book.setPageCount(pageCount);
                        book.setLanguage(language);
                        book.setCost(cost);
                        book.setCountOfBooks(countOfBooks);
                        bookRepository.save(book);
                        logger.debug("Обновлена книга с ISBN {}", isbn);
                    } else {
                        // Создание новой книги
                        Book book = new Book();
                        book.setIsbn(isbn);
                        book.setName(name);
                        book.setPublicationYear(publicationYear);
                        book.setAgeLimit(ageLimit);
                        book.setPublishingCompany(publishingCompany);
                        book.setPageCount(pageCount);
                        book.setLanguage(language);
                        book.setCost(cost);
                        book.setCountOfBooks(countOfBooks);
                        bookRepository.save(book);
                        logger.debug("Создана новая книга с ISBN {}", isbn);
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

    private void importAuthorships() throws IOException, CsvValidationException {
        Path filePath = Paths.get(EXPORT_DIR + "/authorships.csv");
        if (!Files.exists(filePath)) {
            logger.warn("authorships.csv не найден, пропуск импорта авторств.");
            return;
        }

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(
                        new FileInputStream(filePath.toFile()),
                        StandardCharsets.UTF_8
                )
        )) {
            String[] nextLine;
            reader.readNext(); // Пропуск заголовка
            int imported = 0;
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
                            logger.debug("Создано авторство: ISBN={}, AuthorID={}", bookIsbn, authorId);
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

    private void importBookStyles() throws IOException, CsvValidationException {
        Path filePath = Paths.get(EXPORT_DIR + "/book_styles.csv");
        if (!Files.exists(filePath)) {
            logger.warn("book_styles.csv не найден, пропуск импорта стилей книг.");
            return;
        }

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(
                        new FileInputStream(filePath.toFile()),
                        StandardCharsets.UTF_8
                )
        )) {
            String[] nextLine;
            reader.readNext(); // Пропуск заголовка
            int imported = 0;
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
                            logger.debug("Создана связь книги со стилем: ISBN={}, StyleID={}", bookIsbn, styleId);
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
