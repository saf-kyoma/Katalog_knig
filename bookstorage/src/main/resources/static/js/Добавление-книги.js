document.addEventListener('DOMContentLoaded', function() {
  // 1. Очистка нежелательных символов в полях
  const regex = /[^A-Za-zА-Яа-я0-9\s\-]/g;
  const fields = ['name', 'publishingCompany'];

  fields.forEach(function(id) {
    const input = document.getElementById(id);
    if (input) {
      input.addEventListener('input', function() {
        this.value = this.value.replace(regex, '');
      });
    }
  });

  // 2. Ограничение даты до текущего дня
  const today = new Date();
  const day = String(today.getDate()).padStart(2, '0');
  const month = String(today.getMonth() + 1).padStart(2, '0');
  const year = today.getFullYear();
  const formattedDate = `${year}-${month}-${day}`;

  const dateInput = document.getElementById('publicationYear');
  if (dateInput) {
    dateInput.setAttribute('max', formattedDate);
  }

  // 3. Валидация ISBN-13
  const isbnInput = document.getElementById('isbn');

  function formatISBN(value) {
    let digits = value.replace(/\D/g, '');
    if (digits.length > 13) digits = digits.substring(0, 13);
    const parts = [];
    let index = 0;

    if (digits.length > 3) {
      parts.push(digits.substring(0, 3));
      index = 3;
    } else {
      parts.push(digits);
      return parts.join('-');
    }

    if (digits.length > 4) {
      parts.push(digits.substring(3, 4));
      index = 4;
    } else {
      parts.push(digits.substring(3));
      return parts.join('-');
    }

    if (digits.length > 6) {
      parts.push(digits.substring(4, 6));
      index = 6;
    } else {
      parts.push(digits.substring(4));
      return parts.join('-');
    }

    if (digits.length > 12) {
      parts.push(digits.substring(6, 12));
      index = 12;
    } else {
      parts.push(digits.substring(6));
      return parts.join('-');
    }

    if (digits.length > 12) {
      parts.push(digits.substring(12, 13));
    }

    return parts.join('-');
  }

  function onInputISBN(event) {
    const input = event.target;
    const formattedValue = formatISBN(input.value);
    input.value = formattedValue;
    input.setSelectionRange(input.value.length, input.value.length);
  }

  function restrictISBNInput(event) {
    const allowedKeys = ['Backspace', 'ArrowLeft', 'ArrowRight', 'Delete', 'Tab'];
    if (allowedKeys.includes(event.key)) return;
    if (!/^\d$/.test(event.key)) event.preventDefault();
  }

  if (isbnInput) {
    isbnInput.addEventListener('input', onInputISBN);
    isbnInput.addEventListener('keydown', restrictISBNInput);
  }

  // 4. Функциональность для динамического добавления и удаления авторов
  const authorsContainer = document.getElementById('authors-container');
  const addAuthorBtn = document.getElementById('addButtonAuthor');

  addAuthorBtn.addEventListener('click', function() {
    const authorGroup = document.createElement('div');
    authorGroup.classList.add('author-group');

    const removeBtn = document.createElement('span');
    removeBtn.classList.add('remove-author');
    removeBtn.innerHTML = '&times;';
    removeBtn.style.display = 'block';
    removeBtn.addEventListener('click', function() {
      authorsContainer.removeChild(authorGroup);
    });

    authorGroup.appendChild(removeBtn);

    // ФИО автора
    const fioGroup = document.createElement('div');
    fioGroup.classList.add('u-form-group');
    const fioLabel = document.createElement('label');
    fioLabel.setAttribute('for', 'authorFio');
    fioLabel.classList.add('u-label');
    fioLabel.textContent = 'ФИО автора';
    const fioInput = document.createElement('input');
    fioInput.type = 'text';
    fioInput.placeholder = 'Введите ФИО автора';
    fioInput.name = 'authorFio';
    fioInput.classList.add('u-input', 'author-fio');
    fioInput.required = true;
    fioInput.pattern = '[A-Za-zА-Яа-яЁё\\s\\-]+';
    fioInput.title = 'Разрешены только буквы, пробелы и дефис (-)';
    fioGroup.appendChild(fioLabel);
    fioGroup.appendChild(fioInput);

    // Список подсказок для автора
    const suggestionsList = document.createElement('ul');
    suggestionsList.classList.add('author-suggestions-list');
    suggestionsList.style.display = 'none';
    fioGroup.appendChild(suggestionsList);

    // Скрытое поле для хранения ID автора
    const authorIdInput = document.createElement('input');
    authorIdInput.type = 'hidden';
    authorIdInput.name = 'authorId';
    authorIdInput.classList.add('author-id');
    fioGroup.appendChild(authorIdInput);

    authorGroup.appendChild(fioGroup);
    authorsContainer.appendChild(authorGroup);

    // Добавляем обработчики для нового поля ФИО автора
    addAuthorFioListener(fioInput, suggestionsList, authorIdInput);
  });

  // 5. Функциональность для подсказок авторов из базы данных
  function showAuthorSuggestions(inputElement, suggestionsListElement, authorIdInput) {
    const query = inputElement.value.trim();
    if (query.length === 0) {
      suggestionsListElement.innerHTML = '';
      suggestionsListElement.style.display = 'none';
      return;
    }

    console.log(`Запрос к авторам: ${query}`); // Логирование запроса

    // Отправка AJAX-запроса на сервер для поиска авторов
    fetch(`/api/authors/search?q=${encodeURIComponent(query)}`)
      .then(response => {
        console.log('Получен ответ для авторов:', response); // Логирование ответа
        return response.json();
      })
      .then(data => {
        console.log('Данные авторов:', data); // Логирование полученных данных
        suggestionsListElement.innerHTML = '';
        if (data.length === 0) {
          const noResultItem = document.createElement('li');
          noResultItem.textContent = 'Автор не найден. Можно добавить нового.';
          noResultItem.style.cursor = 'default';
          suggestionsListElement.appendChild(noResultItem);
          suggestionsListElement.style.display = 'block';
          return;
        }

        data.forEach(author => {
          const listItem = document.createElement('li');
          listItem.textContent = `${author.fio}${author.nickname ? ' (' + author.nickname + ')' : ''}, ${author.birthDate || '---'}, ${author.country || '---'}`;
          listItem.addEventListener('click', function() {
            inputElement.value = author.fio;
            authorIdInput.value = author.id; // Сохраняем ID автора
            suggestionsListElement.innerHTML = '';
            suggestionsListElement.style.display = 'none';
          });
          suggestionsListElement.appendChild(listItem);
        });
        suggestionsListElement.style.display = 'block';
      })
      .catch(error => {
        console.error('Ошибка при получении подсказок авторов:', error);
      });
  }

  function addAuthorFioListener(inputElement, suggestionsListElement, authorIdInput) {
    inputElement.addEventListener('input', function() {
      showAuthorSuggestions(inputElement, suggestionsListElement, authorIdInput);
    });

    // Закрытие списка подсказок при клике вне поля
    document.addEventListener('click', function(event) {
      if (!inputElement.contains(event.target) && !suggestionsListElement.contains(event.target)) {
        suggestionsListElement.innerHTML = '';
        suggestionsListElement.style.display = 'none';
      }
    });
  }

  // Инициализируем обработчики для существующих полей ФИО автора
  const initialAuthorInputs = document.querySelectorAll('.author-fio');
  const initialAuthorSuggestions = document.querySelectorAll('.author-suggestions-list');
  const initialAuthorIds = document.querySelectorAll('.author-id');

  initialAuthorInputs.forEach((input, index) => {
    const suggestionsListElement = initialAuthorSuggestions[index];
    const authorIdInput = initialAuthorIds[index];
    addAuthorFioListener(input, suggestionsListElement, authorIdInput);
  });

  // 6. Валидация Жанра с Тэгами
  const genreInput = document.getElementById('genre-input');
  const genreTagsContainer = document.getElementById('genre-tags');
  const genresHiddenInput = document.getElementById('genres-hidden'); // Скрытое поле

  // Функция для создания тэга
  function createTag(text) {
    if (!text.trim()) return;

    // Проверка на дублирование
    const existingGenres = Array.from(genreTagsContainer.children).map(tag => tag.firstChild.textContent.trim().toLowerCase());
    if (existingGenres.includes(text.trim().toLowerCase())) return;

    const tag = document.createElement('span');
    tag.classList.add('tag');

    const tagText = document.createElement('span');
    tagText.textContent = text.trim();

    const removeBtn = document.createElement('span');
    removeBtn.classList.add('remove-tag');
    removeBtn.textContent = '×';
    removeBtn.addEventListener('click', function() {
      genreTagsContainer.removeChild(tag);
      updateGenresHiddenInput();
    });

    tag.appendChild(tagText);
    tag.appendChild(removeBtn);
    genreTagsContainer.appendChild(tag);
    updateGenresHiddenInput();
  }

  // Функция для обновления скрытого поля с жанрами
  function updateGenresHiddenInput() {
    const tags = Array.from(genreTagsContainer.children).map(tag => tag.firstChild.textContent);
    genresHiddenInput.value = tags.join(',');
  }

  // Разрешаем ввод только букв и пробелов
  genreInput.addEventListener('input', function() {
    this.value = this.value.replace(/[^A-Za-zА-Яа-яЁё\s]/g, '');
  });

  // Функция для создания списка подсказок на основе запроса
  function showGenreSuggestions(query) {
    if (query.length === 0) {
      removeGenreSuggestions();
      return;
    }

    fetch(`/api/styles/search?q=${encodeURIComponent(query)}`)
      .then(response => {
        if (!response.ok) {
          throw new Error('Сбой при поиске жанров');
        }
        return response.json();
      })
      .then(data => {
        const filteredGenres = data
          .map(style => style.name)
          .filter(genre => !Array.from(genreTagsContainer.children).some(tag => tag.firstChild.textContent.toLowerCase() === genre.toLowerCase()));

        createGenreSuggestionsList(filteredGenres);
      })
      .catch(error => {
        console.error('Ошибка при получении подсказок жанров:', error);
      });
  }

  // Функция для создания списка подсказок
  function createGenreSuggestionsList(filteredGenres) {
    removeGenreSuggestions();

    if (filteredGenres.length === 0) return;

    const list = document.createElement('ul');
    list.id = 'genre-suggestions';
    list.classList.add('suggestions-list');

    filteredGenres.forEach(genre => {
      const listItem = document.createElement('li');
      listItem.textContent = genre;
      listItem.addEventListener('click', function() {
        createTag(genre);
        genreInput.value = '';
        removeGenreSuggestions();
      });
      list.appendChild(listItem);
    });

    genreInput.parentElement.appendChild(list);
  }

  // Функция для удаления списка подсказок
  function removeGenreSuggestions() {
    const existingList = document.getElementById('genre-suggestions');
    if (existingList) {
      existingList.remove();
    }
  }

  // Обработчик ввода в поле жанра
  genreInput.addEventListener('input', function() {
    const query = this.value.trim();
    if (query.length === 0) {
      removeGenreSuggestions();
      return;
    }
    showGenreSuggestions(query);
  });

  // Закрытие подсказок при клике вне поля
  document.addEventListener('click', function (event) {
    if (!genreInput.contains(event.target) && !genreTagsContainer.contains(event.target)) {
      removeGenreSuggestions();
    }
  });

  // Обработка нажатия клавиши Enter для добавления жанра
  genreInput.addEventListener('keydown', function(event) {
    if (event.key === 'Enter') {
      event.preventDefault();
      const genre = this.value.trim();
      if (genre !== '') {
        createTag(genre);
        this.value = '';
        removeGenreSuggestions();
      }
    }
  });

  // 7. Функциональность для отправки формы через AJAX
  const form = document.getElementById('book-add-form');

  form.addEventListener('submit', function(event) {
    event.preventDefault(); // Предотвращаем стандартную отправку формы

    // Собираем данные из формы
    const formData = new FormData(form);

    // ISBN
    const isbn = formData.get('isbn').trim();

    // Название
    const name = formData.get('name').trim();

    // Год публикации
    const publicationYear = formData.get('publicationYear').trim();

    // Возрастное ограничение
    const ageLimit = parseFloat(formData.get('ageLimit'));

    // Издатель
    const publishingCompany = formData.get('publishingCompany').trim();

    // Количество страниц
    const pageCount = parseInt(formData.get('pageCount'), 10);

    // Язык
    const language = formData.get('language').trim();

    // Цена
    const cost = parseFloat(formData.get('cost'));

    // Количество книг
    const countOfBooks = parseInt(formData.get('countOfBooks'), 10);

    // Авторы
    const authorFios = form.querySelectorAll('input[name="authorFio"]');
    const authorIds = form.querySelectorAll('input[name="authorId"]');

    const authors = [];
    for (let i = 0; i < authorFios.length; i++) {
      const author = {
        id: authorIds[i].value.trim() !== '' ? parseInt(authorIds[i].value.trim(), 10) : null,
        fio: authorFios[i].value.trim()
      };
      authors.push(author);
    }

    // Жанры
    const genres = formData.get('genres').split(',').map(genre => genre.trim()).filter(genre => genre.length > 0);

    // Проверка наличия хотя бы одного жанра
    if (genres.length === 0) {
      alert('Пожалуйста, добавьте хотя бы один жанр.');
      return;
    }

    // Формируем объект для отправки
    const bookDTO = {
      isbn: isbn,
      name: name,
      publicationYear: publicationYear,
      ageLimit: ageLimit,
      publishingCompany: publishingCompany,
      pageCount: pageCount,
      language: language,
      cost: cost,
      countOfBooks: countOfBooks,
      authors: authors,
      genres: genres
    };

    console.log('Отправляемые данные:', bookDTO);

    // Отправка данных на сервер через Fetch API
    fetch('/api/books', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(bookDTO)
    })
    .then(response => {
      if (response.ok) {
        document.querySelector('.u-form-send-success').style.display = 'block';
        document.querySelector('.u-form-send-error').style.display = 'none';
        form.reset();
        genreTagsContainer.innerHTML = '';
        genresHiddenInput.value = '';
        const authorGroups = document.querySelectorAll('.author-group');
        authorGroups.forEach((group, index) => {
          if (index > 0) {
            group.remove();
          } else {
            group.querySelectorAll('input.author-fio').forEach(input => input.value = '');
            group.querySelectorAll('input.author-id').forEach(input => input.value = '');
          }
        });
      } else {
        document.querySelector('.u-form-send-success').style.display = 'none';
        document.querySelector('.u-form-send-error').style.display = 'block';
      }
    })
    .catch(error => {
      console.error('Ошибка при отправке формы:', error);
      document.querySelector('.u-form-send-success').style.display = 'none';
      document.querySelector('.u-form-send-error').style.display = 'block';
    });
  });

  // 8. Функциональность для подсказок издательств из базы данных
  function showPublishingCompanySuggestions(inputElement, suggestionsListElement) {
    const query = inputElement.value.trim();
    if (query.length === 0) {
      suggestionsListElement.innerHTML = '';
      suggestionsListElement.style.display = 'none';
      return;
    }

    console.log(`Запрос к издательствам: ${query}`);

    fetch(`/api/publishing-companies/search?q=${encodeURIComponent(query)}`)
      .then(response => {
        if (!response.ok) {
          throw new Error('Сбой при поиске издательств');
        }
        console.log('Получен ответ для издательств:', response);
        return response.json();
      })
      .then(data => {
        console.log('Данные издательств:', data);
        suggestionsListElement.innerHTML = '';
        if (data.length === 0) {
          const noResultItem = document.createElement('li');
          noResultItem.textContent = 'Издательство не найдено. Можно добавить новое.';
          noResultItem.style.cursor = 'default';
          suggestionsListElement.appendChild(noResultItem);
          suggestionsListElement.style.display = 'block';
          return;
        }

        data.forEach(company => {
          const listItem = document.createElement('li');
          listItem.textContent = company.name;
          listItem.addEventListener('click', function() {
            inputElement.value = company.name;
            suggestionsListElement.innerHTML = '';
            suggestionsListElement.style.display = 'none';
          });
          suggestionsListElement.appendChild(listItem);
        });
        suggestionsListElement.style.display = 'block';
      })
      .catch(error => {
        console.error('Ошибка при получении подсказок издательств:', error);
      });
  }

  const publishingCompanyInput = document.getElementById('publishingCompany');
  const publishingCompanySuggestions = document.getElementById('publishingCompany-suggestions');

  if (publishingCompanyInput && publishingCompanySuggestions) {
    publishingCompanyInput.addEventListener('input', function() {
      showPublishingCompanySuggestions(publishingCompanyInput, publishingCompanySuggestions);
    });

    document.addEventListener('click', function(event) {
      if (!publishingCompanyInput.contains(event.target) && !publishingCompanySuggestions.contains(event.target)) {
        publishingCompanySuggestions.innerHTML = '';
        publishingCompanySuggestions.style.display = 'none';
      }
    });
  }
});

// *** ДОБАВЛЕННЫЙ КОД ДЛЯ АВТОРИЗАЦИИ ***
document.addEventListener("DOMContentLoaded", function () {
  // Получаем токен из localStorage (ключ "jwtToken" должен быть установлен после успешного входа)
  const token = localStorage.getItem("jwtToken");

  // Получаем элементы по их id
  const loginLink = document.getElementById("loginLink");
  const logoutMenuItem = document.getElementById("logoutMenuItem");

  // Если токена нет, перенаправляем на страницу входа
  if (!token) {
    alert("Для доступа к странице добавления книги необходимо войти в систему");
    window.location.href = "Каталог.html";
  } else {
    // Если токен присутствует – показываем кнопку выхода и скрываем "Войти"
    if (loginLink) {
      loginLink.style.display = "none";
    }
    if (logoutMenuItem) {
      logoutMenuItem.style.display = "block";
    }
  }

  // Обработчик для кнопки "Выход"
  const logoutButton = document.getElementById("logoutButton");
  if (logoutButton) {
    logoutButton.addEventListener("click", function (e) {
      e.preventDefault();
      localStorage.removeItem("jwtToken");
      alert("Вы вышли из системы.");
      window.location.reload();
    });
  }

  // При необходимости можно добавить дополнительные проверки и логику для элементов этой страницы.
});


document.addEventListener("DOMContentLoaded", function () {
  // Проверяем наличие токена
  const token = localStorage.getItem("jwtToken");
  const dbMenu = document.querySelector(".dropdown[data-bs-toggle='dropdown']");
  const importBtn = document.getElementById("DBadd");
  const exportBtn = document.getElementById("DBout");

  if (!token) {
    // 1. Скрыть весь пункт меню "База данных"
    if (dbMenu) {
      dbMenu.style.pointerEvents = "none";
      dbMenu.style.opacity = "0.5";
      dbMenu.title = "Для доступа к базе данных необходимо войти в систему";
    }

    // 2. Заблокировать кнопки внутри меню
    [importBtn, exportBtn].forEach(btn => {
      if (btn) {
        btn.classList.add("disabled");
        btn.setAttribute("aria-disabled", "true");
        btn.style.pointerEvents = "none";
        btn.style.opacity = "0.5";
        btn.title = "Для выполнения действия необходимо войти в систему";
        btn.style.backgroundColor = "#2cccc4";
                btn.style.color = "#ffffff";
      }
    });
  } else {
    // Если токен есть — разблокировать кнопки и меню
    if (dbMenu) {
      dbMenu.style.pointerEvents = "auto";
      dbMenu.style.opacity = "1";
      dbMenu.title = "";
    }
    [importBtn, exportBtn].forEach(btn => {
      if (btn) {
        btn.classList.remove("disabled");
        btn.removeAttribute("aria-disabled");
        btn.style.pointerEvents = "auto";
        btn.style.opacity = "1";
        btn.title = "";
      }
    });
  }
});
