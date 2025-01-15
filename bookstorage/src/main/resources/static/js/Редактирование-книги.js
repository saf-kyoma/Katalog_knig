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

  // 3. Поле ISBN readonly → проверку пропускаем

  // 4. Функциональность для динамического добавления и удаления авторов
  const authorsContainer = document.getElementById('authors-container');
  const addAuthorBtn = document.getElementById('addAuthorBtn');
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
    const suggestionsList = document.createElement('ul');
    suggestionsList.classList.add('author-suggestions-list');
    suggestionsList.style.display = 'none';
    fioGroup.appendChild(suggestionsList);
    const authorIdInput = document.createElement('input');
    authorIdInput.type = 'hidden';
    authorIdInput.name = 'authorId';
    authorIdInput.classList.add('author-id');
    fioGroup.appendChild(authorIdInput);
    authorGroup.appendChild(fioGroup);
    authorsContainer.appendChild(authorGroup);
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
    console.log(`Запрос к авторам: ${query}`);
    fetch(`/api/authors/search?q=${encodeURIComponent(query)}`)
      .then(response => {
        console.log('Получен ответ для авторов:', response);
        if (!response.ok) {
          throw new Error('Ошибка при поиске авторов');
        }
        return response.json();
      })
      .then(data => {
        console.log('Данные авторов:', data);
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
            authorIdInput.value = author.id;
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
    document.addEventListener('click', function(event) {
      if (!inputElement.contains(event.target) && !suggestionsListElement.contains(event.target)) {
        suggestionsListElement.innerHTML = '';
        suggestionsListElement.style.display = 'none';
      }
    });
  }

  const initialAuthorInputs = document.querySelectorAll('.author-fio');
  const initialAuthorSuggestions = document.querySelectorAll('.author-suggestions-list');
  const initialAuthorIds = document.querySelectorAll('.author-id');
  initialAuthorInputs.forEach((input, index) => {
    const suggestionsListElement = initialAuthorSuggestions[index];
    const authorIdInput = initialAuthorIds[index];
    addAuthorFioListener(input, suggestionsListElement, authorIdInput);
  });

  // 6. Функциональность для работы с жанрами
  const genreInput = document.getElementById('genre-input');
  const genreTagsContainer = document.getElementById('genre-tags');
  function createTag(text) {
    if (!text.trim()) return;
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
    });
    tag.appendChild(tagText);
    tag.appendChild(removeBtn);
    genreTagsContainer.appendChild(tag);
  }
  genreInput.addEventListener('input', function() {
    this.value = this.value.replace(/[^A-Za-zА-Яа-яЁё\s]/g, '');
  });
  genreInput.addEventListener('keydown', function(event) {
    if (event.key === 'Enter') {
      event.preventDefault();
      const genre = this.value.trim();
      if (genre !== '') {
        createTag(genre);
        this.value = '';
      }
    }
  });

  // 7. Интеграция редактирования книги

  function getParameterByName(name) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(name);
  }

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
    genreTagsContainer.innerHTML = '';
    if (book.genres && Array.isArray(book.genres)) {
      book.genres.forEach(genre => {
        createTag(genre);
      });
    }
    const existingAuthorGroups = authorsContainer.querySelectorAll('.author-group');
    existingAuthorGroups.forEach((group, index) => {
      if (index > 0) {
        authorsContainer.removeChild(group);
      }
    });
    const firstAuthor = book.authors[0];
    if (firstAuthor) {
      const firstAuthorInput = authorsContainer.querySelector('.author-fio');
      const firstAuthorIdInput = authorsContainer.querySelector('.author-id');
      firstAuthorInput.value = firstAuthor.fio || '';
      firstAuthorIdInput.value = firstAuthor.id || '';
    }
    for (let i = 1; i < book.authors.length; i++) {
      const author = book.authors[i];
      if (author) {
        addNewAuthor(author.fio, author.id);
      }
    }
  }

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
    const suggestionsList = document.createElement('ul');
    suggestionsList.classList.add('author-suggestions-list');
    suggestionsList.style.display = 'none';
    fioGroup.appendChild(suggestionsList);
    const authorIdInput = document.createElement('input');
    authorIdInput.type = 'hidden';
    authorIdInput.name = 'authorId';
    authorIdInput.classList.add('author-id');
    authorIdInput.value = authorId || '';
    fioGroup.appendChild(authorIdInput);
    authorGroup.appendChild(fioGroup);
    authorsContainer.appendChild(authorGroup);
    addAuthorFioListener(fioInput, suggestionsList, authorIdInput);
    authorCounter++;
  }

  function handleFormSubmit(event) {
    event.preventDefault();
    const form = event.target;
    const formData = new FormData(form);
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
    if (bookData.authors.length === 0) {
      alert('Укажите хотя бы одного автора.');
      return;
    }
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
      const successMsg = document.querySelector('.u-form-send-success');
      successMsg.style.display = 'block';
      successMsg.textContent = 'Спасибо! Ваша книга обновлена.';
      setTimeout(() => {
        successMsg.style.display = 'none';
      }, 3000);
    })
    .catch(error => {
      console.error('Ошибка:', error);
      const errorMsg = document.querySelector('.u-form-send-error');
      errorMsg.style.display = 'block';
      errorMsg.textContent = 'Отправка не удалась. Пожалуйста, исправьте ошибки и попробуйте еще раз.';
      setTimeout(() => {
        errorMsg.style.display = 'none';
      }, 3000);
    });
  }

  const bookEditForm = document.getElementById('book-edit-form');
  if (bookEditForm) {
    bookEditForm.addEventListener('submit', handleFormSubmit);
  }

  const isbnFromURL = getParameterByName('isbn');
  if (isbnFromURL) {
    fetchBookData(isbnFromURL);
  } else {
    alert('ISBN не указан в URL.');
  }

  // 8. Функциональность для подсказок издательств
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

// *** ДОБАВЛЕННЫЙ КОД ДЛЯ КНОПОК ИМПОРТА/ЭКСПОРТА ***
document.addEventListener('DOMContentLoaded', function() {
  const importBtn = document.getElementById('DBadd');
  const exportBtn = document.getElementById('DBout');
  importBtn.addEventListener('click', function() {
    fetch('/api/csv/import', { method: 'POST' })
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
  exportBtn.addEventListener('click', function() {
    fetch('/api/csv/export', { method: 'POST' })
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

// *** ДОБАВЛЕННЫЙ КОД ДЛЯ АВТОРИЗАЦИИ И БЛОКИРОВКИ КНОПОК (Добавить автора, Редактировать, Отмена) ***
document.addEventListener("DOMContentLoaded", function () {
  // Проверяем наличие токена
  const token = localStorage.getItem("jwtToken");
  // Элементы навигации
  const loginLink = document.getElementById("loginLink");
  const logoutMenuItem = document.getElementById("logoutMenuItem");
  if (token) {
    if (loginLink) { loginLink.style.display = "none"; }
    if (logoutMenuItem) { logoutMenuItem.style.display = "block"; }
  } else {
    if (logoutMenuItem) { logoutMenuItem.style.display = "none"; }
    // Блокируем кнопки и перенаправляем на страницу входа
    const addAuthorBtn = document.getElementById("addAuthorBtn");
    const editBookSubmitBtn = document.getElementById("editBookSubmitBtn");
    const cancelButton = document.getElementById("cancelButton");
    if (addAuthorBtn) {
      addAuthorBtn.classList.add("disabled");
      addAuthorBtn.setAttribute("aria-disabled", "true");
      addAuthorBtn.style.pointerEvents = "none";
      addAuthorBtn.style.opacity = "0.5";
      addAuthorBtn.title = "Для добавления автора необходимо войти в систему";
      addAuthorBtn.style.backgroundColor = "#2cccc4";
      addAuthorBtn.style.color = "#ffffff";
    }
    if (editBookSubmitBtn) {
      editBookSubmitBtn.classList.add("disabled");
      editBookSubmitBtn.setAttribute("aria-disabled", "true");
      editBookSubmitBtn.style.pointerEvents = "none";
      editBookSubmitBtn.style.opacity = "0.5";
      editBookSubmitBtn.title = "Для редактирования книги необходимо войти в систему";
      editBookSubmitBtn.style.backgroundColor = "#2cccc4";
      editBookSubmitBtn.style.color = "#ffffff";
    }
    if (cancelButton) {
      cancelButton.classList.add("disabled");
      cancelButton.setAttribute("aria-disabled", "true");
      cancelButton.style.pointerEvents = "none";
      cancelButton.style.opacity = "0.5";
      cancelButton.title = "Для доступа к этой странице необходимо войти в систему";
      cancelButton.style.backgroundColor = "#2cccc4";
      cancelButton.style.color = "#ffffff";
    }
    alert("Для доступа к странице редактирования книги необходимо войти в систему.");
    window.location.href = "Вход-в-систему.html";
    return;
  }

  // Если токен есть — разблокируем кнопки (на случай, если ранее были заблокированы)
  const addAuthorBtn = document.getElementById("addAuthorBtn");
  const editBookSubmitBtn = document.getElementById("editBookSubmitBtn");
  const cancelButton = document.getElementById("cancelButton");
  if (addAuthorBtn) {
    addAuthorBtn.classList.remove("disabled");
    addAuthorBtn.removeAttribute("aria-disabled");
    addAuthorBtn.style.pointerEvents = "auto";
    addAuthorBtn.style.opacity = "1";
    addAuthorBtn.title = "";
    addAuthorBtn.style.backgroundColor = "";
    addAuthorBtn.style.color = "";
  }
  if (editBookSubmitBtn) {
    editBookSubmitBtn.classList.remove("disabled");
    editBookSubmitBtn.removeAttribute("aria-disabled");
    editBookSubmitBtn.style.pointerEvents = "auto";
    editBookSubmitBtn.style.opacity = "1";
    editBookSubmitBtn.title = "";
    editBookSubmitBtn.style.backgroundColor = "";
    editBookSubmitBtn.style.color = "";
  }
  if (cancelButton) {
    cancelButton.classList.remove("disabled");
    cancelButton.removeAttribute("aria-disabled");
    cancelButton.style.pointerEvents = "auto";
    cancelButton.style.opacity = "1";
    cancelButton.title = "";
    cancelButton.style.backgroundColor = "";
    cancelButton.style.color = "";
  }

  // Обработчик для кнопки "Выход"
  const logoutButton = document.getElementById("logoutButton");
  if (logoutButton) {
    logoutButton.addEventListener("click", function(e) {
      e.preventDefault();
      localStorage.removeItem("jwtToken");
      alert("Вы вышли из системы.");
      window.location.reload();
    });
  }
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

