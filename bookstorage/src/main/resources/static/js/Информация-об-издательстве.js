// Функция для получения параметра из URL
function getParameterByName(name) {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get(name);
}

// Заполняет информацию об издательстве
function populatePublisherInfo(publisher) {
  document.getElementById('publisher-name-cell').textContent = publisher.name || 'Не указано';
  document.getElementById('publisher-year-cell').textContent = publisher.establishmentYear || 'Не указано';
  document.getElementById('publisher-contact-cell').textContent = publisher.contactInfo || 'Не указано';
  document.getElementById('publisher-city-cell').textContent = publisher.city || 'Не указано';
  // Обновляем ссылку для редактирования, передавая имя издательства
  document.getElementById('editPublisherBtn').href = 'Редактирование-издателя.html?name=' + encodeURIComponent(publisher.name);
}

// Запрашивает данные издательства по имени (имя передаётся в URL как параметр name)
function fetchPublisherData(publisherName) {
  fetch(`/api/publishing-companies/${encodeURIComponent(publisherName)}`)
    .then(response => {
      if (!response.ok) {
        throw new Error('Издательство не найдено');
      }
      return response.json();
    })
    .then(publisher => {
      populatePublisherInfo(publisher);
    })
    .catch(error => {
      console.error('Ошибка при загрузке издательства:', error);
      alert('Не удалось загрузить информацию об издательстве.');
    });
}

// Получает список книг через API и фильтрует по издательству (сравнивая название издательства)
function fetchBooks(publisherName, search = '', sortColumn = 'name', sortOrder = 'asc') {
  let apiUrl = '/api/books';
  const params = new URLSearchParams();
  if (search) {
    params.append('search', search);
  }
  params.append('sort_column', sortColumn);
  params.append('sort_order', sortOrder);
  if ([...params].length > 0) {
    apiUrl += '?' + params.toString();
  }
  fetch(apiUrl)
    .then(response => {
      if (!response.ok) {
        throw new Error('Ошибка получения книг: ' + response.statusText);
      }
      return response.json();
    })
    .then(books => {
      // Фильтруем книги: оставляем те, где publishingCompany совпадает с именем издательства
      const filtered = books.filter(book =>
        book.publishingCompany && book.publishingCompany === publisherName
      );
      populateBooksTable(filtered);
    })
    .catch(error => {
      console.error('Ошибка при загрузке книг:', error);
      alert('Не удалось загрузить книги издательства.');
    });
}

// Заполняет таблицу книг
function populateBooksTable(books) {
  const tableBody = document.getElementById('books-table-body');
  tableBody.innerHTML = '';
  if (books.length === 0) {
    const row = document.createElement('tr');
    const cell = document.createElement('td');
    cell.colSpan = 7;
    cell.className = 'text-center';
    cell.textContent = 'Книги не найдены.';
    row.appendChild(cell);
    tableBody.appendChild(row);
    return;
  }
  books.forEach(book => {
    const row = document.createElement('tr');
    // Чекбокс
    const checkboxTd = document.createElement('td');
    checkboxTd.classList.add('checkbox-column');
    const checkbox = document.createElement('input');
    checkbox.type = 'checkbox';
    checkbox.value = book.isbn;
    checkboxTd.appendChild(checkbox);
    row.appendChild(checkboxTd);
    // Название книги (ссылка)
    const nameTd = document.createElement('td');
    const link = document.createElement('a');
    link.href = `Информация-о-книге.html?isbn=${book.isbn}`;
    link.textContent = book.name || 'Без названия';
    link.className = 'text-decoration-none';
    link.style.color = '#0d6efd';
    nameTd.appendChild(link);
    row.appendChild(nameTd);
    // Автор(ы)
    const authorTd = document.createElement('td');
    if (book.authors && Array.isArray(book.authors)) {
      authorTd.textContent = book.authors.map(a => a.fio).join(', ');
    } else {
      authorTd.textContent = 'Не указано';
    }
    row.appendChild(authorTd);
    // Дата издания
    const yearTd = document.createElement('td');
    yearTd.textContent = book.publicationYear || 'Не указано';
    row.appendChild(yearTd);
    // Издательство
    const pubTd = document.createElement('td');
    pubTd.textContent = book.publishingCompany || 'Не указано';
    row.appendChild(pubTd);
    // Количество экземпляров
    const countTd = document.createElement('td');
    countTd.textContent = (book.countOfBooks !== undefined) ? book.countOfBooks : 'Не указано';
    row.appendChild(countTd);
    // ISBN
    const isbnTd = document.createElement('td');
    isbnTd.textContent = book.isbn || 'Не указано';
    row.appendChild(isbnTd);
    tableBody.appendChild(row);
  });
}

// Исправленная функция удаления издательства с использованием bulk-delete (массив из одного элемента)
function deletePublisher(publisherName) {
  if (!confirm('Вы уверены, что хотите удалить это издательство? Все книги этого издательства также будут удалены!')) return;
  fetch('/api/publishing-companies/bulk-delete', {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify([publisherName])
  })
  .then(response => {
    if (response.status === 204) {
      alert('Издательство успешно удалено.');
      window.location.href = 'Страница-издательств.html';
    } else {
      throw new Error('Ошибка при удалении издательства');
    }
  })
  .catch(error => {
    console.error('Ошибка при удалении издательства:', error);
    alert('Не удалось удалить издательство.');
  });
}

// Множественное удаление книг
function deleteSelectedBooks(publisherName) {
  const selectedCheckboxes = document.querySelectorAll('#books-table-body input[type="checkbox"]:checked');
  if (selectedCheckboxes.length === 0) {
    alert('Выберите хотя бы одну книгу для удаления.');
    return;
  }
  if (!confirm(`Вы уверены, что хотите удалить ${selectedCheckboxes.length} книгу(и)?`)) return;
  const isbns = Array.from(selectedCheckboxes).map(cb => cb.value);
  fetch('/api/books/bulk-delete', {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(isbns)
  })
  .then(response => {
    if (response.status === 204) {
      alert('Выбранные книги успешно удалены.');
      const searchValue = document.getElementById('searchBookInput').value.trim();
      fetchBooks(publisherName, searchValue);
    } else {
      throw new Error('Ошибка при удалении книг.');
    }
  })
  .catch(error => {
    console.error('Ошибка при удалении книг:', error);
    alert('Произошла ошибка при удалении книг.');
  });
}

// Функция для выбора/снятия всех чекбоксов
function toggleAllCheckboxes(source) {
  const checkboxes = document.querySelectorAll('#books-table-body input[type="checkbox"]');
  checkboxes.forEach(cb => cb.checked = source.checked);
}
window.toggleAllCheckboxes = toggleAllCheckboxes;

// Инициализация обработчиков событий после загрузки документа
document.addEventListener('DOMContentLoaded', function() {
  const publisherName = getParameterByName('name');
  if (!publisherName) {
    alert('Имя издательства не указано в URL.');
    return;
  }
  // Загружаем данные издательства
  fetchPublisherData(publisherName);
  // Загружаем список книг и фильтруем по издательству
  fetchBooks(publisherName);
  // Обработчик удаления издательства
  document.getElementById('deletePublisherBtn').addEventListener('click', function() {
    deletePublisher(publisherName);
  });
  // Обработчик поиска книг
  document.getElementById('searchButton').addEventListener('click', function(e) {
    e.preventDefault();
    const searchValue = document.getElementById('searchBookInput').value.trim();
    fetchBooks(publisherName, searchValue);
  });
  // Обработчик сброса поиска
  document.getElementById('resetButton').addEventListener('click', function(e) {
    e.preventDefault();
    document.getElementById('searchBookInput').value = '';
    fetchBooks(publisherName);
  });
  // Обработчик множественного удаления книг
  document.getElementById('deleteBooksBtn').addEventListener('click', function(e) {
    e.preventDefault();
    deleteSelectedBooks(publisherName);
  });
  // Обработчики сортировки книг
  const sortButtons = document.querySelectorAll('.sort-button');
  sortButtons.forEach(button => {
    button.addEventListener('click', function() {
      const sortColumn = button.getAttribute('data-column');
      let currentOrder = button.getAttribute('data-order') || 'asc';
      const newOrder = currentOrder === 'asc' ? 'desc' : 'asc';
      button.setAttribute('data-order', newOrder);
      button.textContent = newOrder === 'asc' ? '▲' : '▼';
      const searchValue = document.getElementById('searchBookInput').value.trim();
      fetchBooks(publisherName, searchValue, sortColumn, newOrder);
    });
  });
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

// *** ДОБАВЛЕННЫЙ КОД ДЛЯ АВТОРИЗАЦИИ И БЛОКИРОВКИ КНОПОК (Редактировать, Удалить, Удалить выбранные) ***
document.addEventListener("DOMContentLoaded", function () {
  // Получаем токен из localStorage (токен сохраняется после успешного входа под ключом "jwtToken")
  const token = localStorage.getItem("jwtToken");

  // Получаем элементы по id
  const loginLink = document.getElementById("loginLink");
  const logoutMenuItem = document.getElementById("logoutMenuItem");

  if (token) {
    // Если токен найден – пользователь авторизован:
    if (loginLink) {
      loginLink.style.display = "none";
    }
    if (logoutMenuItem) {
      logoutMenuItem.style.display = "block";
    }
  } else {
    // Если токена нет – пользователь не авторизован:
    if (logoutMenuItem) {
      logoutMenuItem.style.display = "none";
    }
  }

  // Блокировка кнопок "Редактировать", "Удалить" издательство и "Удалить выбранные" книги, если нет токена
  const editButton = document.getElementById("editPublisherBtn");
  const deletePublisherBtn = document.getElementById("deletePublisherBtn");
  const deleteBooksBtn = document.getElementById("deleteBooksBtn");

  if (!token) {
    if (editButton) {
      editButton.classList.add("disabled");
      editButton.setAttribute("aria-disabled", "true");
      editButton.style.pointerEvents = "none";
      editButton.style.opacity = "0.5";
      editButton.title = "Для редактирования издательства необходимо войти в систему";
      editButton.style.backgroundColor = "#2cccc4";
      editButton.style.color = "#ffffff";
    }
    if (deletePublisherBtn) {
      deletePublisherBtn.classList.add("disabled");
      deletePublisherBtn.setAttribute("aria-disabled", "true");
      deletePublisherBtn.style.pointerEvents = "none";
      deletePublisherBtn.style.opacity = "0.5";
      deletePublisherBtn.title = "Для удаления издательства необходимо войти в систему";
      deletePublisherBtn.style.backgroundColor = "#2cccc4";
      deletePublisherBtn.style.color = "#ffffff";
    }
    if (deleteBooksBtn) {
      deleteBooksBtn.classList.add("disabled");
      deleteBooksBtn.setAttribute("aria-disabled", "true");
      deleteBooksBtn.style.pointerEvents = "none";
      deleteBooksBtn.style.opacity = "0.5";
      deleteBooksBtn.title = "Для удаления книг необходимо войти в систему";
      deleteBooksBtn.style.backgroundColor = "#2cccc4";
      deleteBooksBtn.style.color = "#ffffff";
    }
    // Если пользователь перешёл по прямой ссылке к странице редактирования и не авторизован, можно (при необходимости) выполнить редирект:
    // alert("Для доступа к этой странице необходимо войти в систему.");
    // window.location.href = "Вход-в-систему.html";
  } else {
    // Если токен есть, разблокируем кнопки (если ранее были заблокированы)
    if (editButton) {
      editButton.classList.remove("disabled");
      editButton.removeAttribute("aria-disabled");
      editButton.style.pointerEvents = "auto";
      editButton.style.opacity = "1";
      editButton.title = "";
      editButton.style.backgroundColor = "";
      editButton.style.color = "";
    }
    if (deletePublisherBtn) {
      deletePublisherBtn.classList.remove("disabled");
      deletePublisherBtn.removeAttribute("aria-disabled");
      deletePublisherBtn.style.pointerEvents = "auto";
      deletePublisherBtn.style.opacity = "1";
      deletePublisherBtn.title = "";
      deletePublisherBtn.style.backgroundColor = "";
      deletePublisherBtn.style.color = "";
    }
    if (deleteBooksBtn) {
      deleteBooksBtn.classList.remove("disabled");
      deleteBooksBtn.removeAttribute("aria-disabled");
      deleteBooksBtn.style.pointerEvents = "auto";
      deleteBooksBtn.style.opacity = "1";
      deleteBooksBtn.title = "";
      deleteBooksBtn.style.backgroundColor = "";
      deleteBooksBtn.style.color = "";
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

