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

    // 3. Удалена валидация ISBN-13, так как поле readonly

    // 4. Функциональность для динамического добавления и удаления авторов
    const authorsContainer = document.getElementById('authors-container');
    const addAuthorBtn = document.querySelector('.add-author-btn');
    let authorCounter = 1; // Счетчик для уникальных ID авторов

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

      const uniqueId = `authorFio${authorCounter}`;

      const fioLabel = document.createElement('label');
      fioLabel.setAttribute('for', uniqueId);
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
      fioInput.id = uniqueId;

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

      authorCounter++;
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
          if (!response.ok) {
            throw new Error('Ошибка при поиске авторов');
          }
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
            // Используем 'fio' и добавляем 'nickname', если он есть
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

    // Функция для обновления скрытого поля с жанрами (если необходимо)
    function updateGenresHiddenInput() {
      // Если вы всё ещё хотите хранить жанры в скрытом поле, но теперь как массив
      const genres = Array.from(genreTagsContainer.children).map(tag => tag.firstChild.textContent);
      // Здесь вы можете сохранить их в скрытом поле как JSON строку, если нужно
      // document.getElementById('genres-hidden').value = JSON.stringify(genres);
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
      // Удаление предыдущих подсказок
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
        if (genre !== '') { // Разрешаем добавление любых жанров, вне зависимости от списка
          createTag(genre);
          this.value = '';
          removeGenreSuggestions();
        }
      }
    });

    ////// ИНТЕГРАЦИЯ РЕДАКТИРОВАНИЯ //////

    // Функция для получения параметра из URL
    function getParameterByName(name) {
      const urlParams = new URLSearchParams(window.location.search);
      return urlParams.get(name);
    }

    // Функция для получения данных книги по ISBN
    function fetchBookData(isbn) {
      fetch(`/api/books/${encodeURIComponent(isbn)}`)
        .then(response => {
          if (!response.ok) {
            throw new Error('Книга не найдена');
          }
          return response.json();
        })
        .then(data => {
          populateForm(data);
        })
        .catch(error => {
          console.error('Ошибка при загрузке данных книги:', error);
          alert('Не удалось загрузить данные книги. Проверьте ISBN и попробуйте снова.');
        });
    }

    // Функция для заполнения формы данными книги
    function populateForm(book) {
      document.getElementById('isbn').value = book.isbn || '';
      document.getElementById('name').value = book.name || '';
      document.getElementById('publicationYear').value = book.publicationYear ? book.publicationYear.substring(0,10) : '';
      document.getElementById('ageLimit').value = book.ageLimit || '';
      document.getElementById('publishingCompany').value = book.publishingCompany || '';
      document.getElementById('pageCount').value = book.pageCount || 1;
      document.getElementById('language').value = book.language || '';
      document.getElementById('cost').value = book.cost || 0;
      document.getElementById('countOfBooks').value = book.countOfBooks || 0;

      // Очистка существующих жанров перед добавлением
      genreTagsContainer.innerHTML = '';

      // Заполнение жанров
      if (book.genres && Array.isArray(book.genres)) {
        book.genres.forEach(genre => {
          createTag(genre);
        });
      }

      // Очистка существующих авторов перед добавлением
      // Оставляем только первый автор и удаляем остальные
      const existingAuthorGroups = authorsContainer.querySelectorAll('.author-group');
      existingAuthorGroups.forEach((group, index) => {
        if (index > 0) {
          authorsContainer.removeChild(group);
        }
      });

      // Заполнение первого автора
      const firstAuthor = book.authors[0];
      if (firstAuthor) {
        const firstAuthorInput = authorsContainer.querySelector('.author-fio');
        const firstAuthorIdInput = authorsContainer.querySelector('.author-id');
        firstAuthorInput.value = firstAuthor.fio || '';
        firstAuthorIdInput.value = firstAuthor.id || '';
      }

      // Добавляем остальных авторов
      for (let i = 1; i < book.authors.length; i++) {
        const author = book.authors[i];
        if (author) {
          addNewAuthor(author.fio, author.id);
        }
      }
    }

    // Функция для добавления нового автора в форму
    function addNewAuthor(fio, authorId) {
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

      const uniqueId = `authorFio${authorCounter}`;

      const fioLabel = document.createElement('label');
      fioLabel.setAttribute('for', uniqueId);
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
      fioInput.id = uniqueId;
      fioInput.value = fio || '';

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
      authorIdInput.value = authorId || '';
      fioGroup.appendChild(authorIdInput);

      authorGroup.appendChild(fioGroup);

      authorsContainer.appendChild(authorGroup);

      // Добавляем обработчики для нового поля ФИО автора
      addAuthorFioListener(fioInput, suggestionsList, authorIdInput);

      authorCounter++;
    }

    // Функция для обработки отправки формы
    function handleFormSubmit(event) {
      event.preventDefault();

      const form = event.target;
      const formData = new FormData(form);

      // Собираем данные книги
      const bookData = {
        isbn: formData.get('isbn'),
        name: formData.get('name'),
        publicationYear: formData.get('publicationYear'),
        ageLimit: formData.get('ageLimit'),
        publishingCompany: formData.get('publishingCompany'),
        pageCount: parseInt(formData.get('pageCount')),
        language: formData.get('language'),
        cost: parseFloat(formData.get('cost')),
        countOfBooks: parseInt(formData.get('countOfBooks')),
        genres: Array.from(genreTagsContainer.children).map(tag => tag.firstChild.textContent),
        authors: []
      };

      // Собираем авторов
      const authorFioInputs = form.querySelectorAll('.author-fio');
      const authorIdInputs = form.querySelectorAll('.author-id');

      authorFioInputs.forEach((fioInput, index) => {
        const fio = fioInput.value.trim();
        const authorId = authorIdInputs[index].value.trim();
        if (fio !== '') {
          bookData.authors.push({
            fio: fio,
            id: authorId || null
          });
        }
      });

      // Валидация наличия хотя бы одного автора
      if (bookData.authors.length === 0) {
        alert('Укажите хотя бы одного автора.');
        return;
      }

      // Отправка данных на сервер
      fetch(`/api/books/${encodeURIComponent(bookData.isbn)}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(bookData)
      })
        .then(response => {
          if (!response.ok) {
            throw new Error('Ошибка при обновлении книги');
          }
          return response.json();
        })
        .then(data => {
          // Отображение успешного сообщения
          const successMsg = document.querySelector('.u-form-send-success');
          successMsg.style.display = 'block';
          successMsg.textContent = 'Спасибо! Ваша книга обновлена.';
          // Скрытие сообщения через 3 секунды
          setTimeout(() => {
            successMsg.style.display = 'none';
          }, 3000);
        })
        .catch(error => {
          console.error('Ошибка:', error);
          // Отображение ошибки
          const errorMsg = document.querySelector('.u-form-send-error');
          errorMsg.style.display = 'block';
          errorMsg.textContent = 'Отправка не удалась. Пожалуйста, исправьте ошибки и попробуйте еще раз.';
          // Скрытие сообщения через 3 секунды
          setTimeout(() => {
            errorMsg.style.display = 'none';
          }, 3000);
        });
    }

    // Добавляем обработчик отправки формы
    const bookEditForm = document.getElementById('book-edit-form');
    if (bookEditForm) {
      bookEditForm.addEventListener('submit', handleFormSubmit);
    }

    ////// ИНТЕГРАЦИЯ ПОДТЯГИВАНИЯ ДАННЫХ //////

    // Получаем ISBN из URL
    const isbnFromURL = getParameterByName('isbn');
    if (isbnFromURL) {
      fetchBookData(isbnFromURL);
    } else {
      alert('ISBN не указан в URL.');
    }

    // 8. Функциональность для подсказок издательств из базы данных
    function showPublishingCompanySuggestions(inputElement, suggestionsListElement) {
      const query = inputElement.value.trim();
      if (query.length === 0) {
        suggestionsListElement.innerHTML = '';
        suggestionsListElement.style.display = 'none';
        return;
      }

      console.log(`Запрос к издательствам: ${query}`); // Логирование запроса

      // Отправка AJAX-запроса на сервер для поиска издательств
      fetch(`/api/publishing-companies/search?q=${encodeURIComponent(query)}`)
        .then(response => {
          if (!response.ok) {
            throw new Error('Сбой при поиске издательств');
          }
          console.log('Получен ответ для издательств:', response); // Логирование ответа
          return response.json();
        })
        .then(data => {
          console.log('Данные издательств:', data); // Логирование полученных данных
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

    // Добавляем обработчик для поля издательства
    const publishingCompanyInput = document.getElementById('publishingCompany');
    const publishingCompanySuggestions = document.getElementById('publishingCompany-suggestions');

    if (publishingCompanyInput && publishingCompanySuggestions) {
      publishingCompanyInput.addEventListener('input', function() {
        showPublishingCompanySuggestions(publishingCompanyInput, publishingCompanySuggestions);
      });

      // Закрытие списка подсказок при клике вне поля
      document.addEventListener('click', function(event) {
        if (!publishingCompanyInput.contains(event.target) && !publishingCompanySuggestions.contains(event.target)) {
          publishingCompanySuggestions.innerHTML = '';
          publishingCompanySuggestions.style.display = 'none';
        }
      });
    }
  });

  // *** ДОБАВЛЕННЫЙ КОД ДЛЯ КНОПОК ИМПОРТА/ЭКСПОРТА ***
      document.addEventListener('DOMContentLoaded', function() {
        const importBtn = document.getElementById('DBadd');
        const exportBtn = document.getElementById('DBout');

        // При нажатии на "Загрузить базу данных" (import)
        importBtn.addEventListener('click', function() {
          fetch('/api/csv/import', {
            method: 'POST'
          })
          .then(response => {
            if (!response.ok) {
              throw new Error('Ошибка при импорте базы данных из CSV');
            }
            return response.text();
          })
          .then(text => {
            alert('Импорт успешно завершён: ' + text);
          })
          .catch(error => {
            console.error('Ошибка:', error);
            alert('Не удалось выполнить импорт: ' + error.message);
          });
        });

        // При нажатии на "Выгрузить базу данных" (export)
        exportBtn.addEventListener('click', function() {
          fetch('/api/csv/export', {
            method: 'POST'
          })
          .then(response => {
            if (!response.ok) {
              throw new Error('Ошибка при экспорте базы данных в CSV');
            }
            return response.text();
          })
          .then(text => {
            alert('Экспорт успешно завершён: ' + text);
          })
          .catch(error => {
            console.error('Ошибка:', error);
            alert('Не удалось выполнить экспорт: ' + error.message);
          });
        });
      });