 // --- Функция для выбора/снятия всех чекбоксов ---
  function toggleAllCheckboxes(source) {
    const checkboxes = document.querySelectorAll('#authors-table-body input[type="checkbox"]');
    checkboxes.forEach(checkbox => checkbox.checked = source.checked);
  }

  // --- Код для импорта/экспорта ---
  document.addEventListener('DOMContentLoaded', function() {
    const importBtn = document.getElementById('DBadd');
    const exportBtn = document.getElementById('DBout');

    if (importBtn) {
      importBtn.addEventListener('click', function() {
        fetch('/api/csv/import', { method: 'POST' })
          .then(response => {
            if (!response.ok) {
              throw new Error('Ошибка при импорте базы данных из CSV');
            }
            return response.text();
          })
          .then(text => alert('Импорт успешно завершён: ' + text))
          .catch(error => {
            console.error('Ошибка:', error);
            alert('Не удалось выполнить импорт: ' + error.message);
          });
      });
    }

    if (exportBtn) {
      exportBtn.addEventListener('click', function() {
        fetch('/api/csv/export', { method: 'POST' })
          .then(response => {
            if (!response.ok) {
              throw new Error('Ошибка при экспорте базы данных в CSV');
            }
            return response.text();
          })
          .then(text => alert('Экспорт успешно завершён: ' + text))
          .catch(error => {
            console.error('Ошибка:', error);
            alert('Не удалось выполнить экспорт: ' + error.message);
          });
      });
    }
  });

  // --- Код для сортировки, поиска и заполнения таблицы ---
  document.addEventListener('DOMContentLoaded', function() {
    let currentSortColumn = 'fio';
    let currentSortOrder = 'asc';
    let currentSearch = '';

    function getSearchValue() {
      const searchInput = document.querySelector('.input-group input[name="search"]');
      return searchInput ? searchInput.value.trim() : '';
    }

    function handleSearch(event) {
      event.preventDefault();
      currentSearch = getSearchValue();
      currentSortColumn = 'fio';
      currentSortOrder = 'asc';
      resetSortButtons();
      fetchAuthors({
        q: currentSearch,
        sort_column: currentSortColumn,
        sort_order: currentSortOrder
      });
    }

    function handleResetButton() {
      currentSearch = '';
      const searchInput = document.querySelector('.input-group input[name="search"]');
      if (searchInput) { searchInput.value = ''; }
      currentSortColumn = 'fio';
      currentSortOrder = 'asc';
      resetSortButtons();
      fetchAuthors({
        sort_column: currentSortColumn,
        sort_order: currentSortOrder
      });
    }

    function resetSortButtons() {
      const sortButtons = document.querySelectorAll('.sort-button');
      sortButtons.forEach(button => {
        button.setAttribute('data-order', 'asc');
        button.textContent = '▲';
        button.classList.remove('btn-secondary');
        button.classList.add('btn-light');
      });
    }

    function fetchAuthors(params = {}) {
      const queryParams = new URLSearchParams(params).toString();
      const apiUrl = params.q ?
          `/api/authors/search?q=${encodeURIComponent(params.q)}&sort_column=${params.sort_column}&sort_order=${params.sort_order}` :
          `/api/authors?sort_column=${params.sort_column}&sort_order=${params.sort_order}`;
      fetch(apiUrl)
        .then(response => {
          if (!response.ok) {
            throw new Error(`Ошибка: ${response.status} ${response.statusText}`);
          }
          return response.json();
        })
        .then(authors => {
          populateAuthorsTable(authors);
        })
        .catch(error => {
          console.error('Ошибка при получении авторов:', error);
          showMessage('Не удалось загрузить список авторов.', 'error');
        });
    }

    function populateAuthorsTable(authors) {
      const tbody = document.getElementById('authors-table-body');
      tbody.innerHTML = '';

      if (authors.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center">Авторы не найдены.</td></tr>';
        return;
      }

      authors.forEach(author => {
        const tr = document.createElement('tr');

        const checkboxTd = document.createElement('td');
        checkboxTd.classList.add('checkbox-column');
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.value = author.id;
        checkboxTd.appendChild(checkbox);
        tr.appendChild(checkboxTd);

        const fioTd = document.createElement('td');
        fioTd.classList.add('u-table-cell');
        const link = document.createElement('a');
        link.href = `Информация-об-авторе.html?id=${author.id}`;
        link.textContent = author.fio || 'Без ФИО';
        link.classList.add('text-decoration-none', 'text-primary');
        fioTd.appendChild(link);
        tr.appendChild(fioTd);

        const birthDateTd = document.createElement('td');
        birthDateTd.classList.add('u-table-cell');
        birthDateTd.textContent = author.birthDate || 'Не указано';
        tr.appendChild(birthDateTd);

        const countryTd = document.createElement('td');
        countryTd.classList.add('u-table-cell');
        countryTd.textContent = author.country || 'Не указано';
        tr.appendChild(countryTd);

        const nicknameTd = document.createElement('td');
        nicknameTd.classList.add('u-table-cell');
        nicknameTd.textContent = author.nickname || 'Не указано';
        tr.appendChild(nicknameTd);

        tbody.appendChild(tr);
      });
    }

    function showMessage(message, type) {
      const messageContainer = document.getElementById('message-container');
      messageContainer.innerHTML = `<div class="message ${type}">${message}</div>`;
      setTimeout(() => { messageContainer.innerHTML = ''; }, 5000);
    }

    function handleSort(column, button) {
      const currentOrder = button.getAttribute('data-order') || 'asc';
      const newOrder = currentOrder === 'asc' ? 'desc' : 'asc';
      button.setAttribute('data-order', newOrder);
      button.textContent = newOrder === 'asc' ? '▲' : '▼';

      const sortButtons = document.querySelectorAll('.sort-button');
      sortButtons.forEach(btn => {
        if (btn !== button) {
          btn.setAttribute('data-order', 'asc');
          btn.textContent = '▲';
          btn.classList.remove('btn-secondary');
          btn.classList.add('btn-light');
        }
      });

      if (newOrder === 'asc') {
        button.classList.remove('btn-secondary');
        button.classList.add('btn-light');
      } else {
        button.classList.remove('btn-light');
        button.classList.add('btn-secondary');
      }

      currentSortColumn = column;
      currentSortOrder = newOrder;
      fetchAuthors({
        q: currentSearch,
        sort_column: currentSortColumn,
        sort_order: currentSortOrder
      });
    }

    function handleDeleteSelectedAuthors(event) {
      event.preventDefault();
      const selectedCheckboxes = document.querySelectorAll('#authors-table-body input[type="checkbox"]:checked');
      if (selectedCheckboxes.length === 0) {
        showMessage('Пожалуйста, выберите хотя бы одного автора для удаления.', 'error');
        return;
      }
      const removeEverything = confirm(
        'Удалить выбранных авторов вместе со всеми их книгами (если у книги нет других соавторов)?\n\nНажмите ОК для удаления ВСЁ, нажмите Отмена — чтобы отменить.'
      );
      const authorIds = Array.from(selectedCheckboxes).map(ch => parseInt(ch.value, 10));
      fetch(`/api/authors/bulk-delete?removeEverything=${removeEverything}`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(authorIds)
      })
      .then(response => {
        if (response.status === 204) {
          if (removeEverything) {
            showMessage('Выбранные авторы и их книги успешно удалены.', 'success');
            fetchAuthors({
              q: currentSearch,
              sort_column: currentSortColumn,
              sort_order: currentSortOrder
            });
          } else {
            showMessage('Удаление отменено пользователем. Ничего не произошло.', 'success');
          }
        } else {
          showMessage('Произошла ошибка при удалении авторов.', 'error');
        }
      })
      .catch(error => {
        console.error('Ошибка при удалении авторов:', error);
        showMessage('Не удалось удалить авторов.', 'error');
      });
    }

    function initializeEventListeners() {
      const searchForm = document.getElementById('searchForm');
      if (searchForm) { searchForm.addEventListener('submit', handleSearch); }

      const searchButton = document.getElementById('searchButton');
      if (searchButton) { searchButton.addEventListener('click', handleSearch); }

      const resetButton = document.getElementById('resetButton');
      if (resetButton) { resetButton.addEventListener('click', handleResetButton); }

      const sortButtons = document.querySelectorAll('.sort-button');
      sortButtons.forEach(button => {
        const columnName = button.getAttribute('data-column');
        button.addEventListener('click', () => handleSort(columnName, button));
      });

      const deleteButton = document.getElementById('deleteSelectedButton');
      if (deleteButton) { deleteButton.addEventListener('click', handleDeleteSelectedAuthors); }
    }

    // Инициализация обработки сортировки, поиска и заполнения таблицы
    initializeEventListeners();
    fetchAuthors();
  });

 // Обработчик для управления авторизацией на странице "Авторы"
document.addEventListener("DOMContentLoaded", function () {
  const token = localStorage.getItem("jwtToken");

  const loginLink = document.getElementById("loginLink");
  const logoutMenuItem = document.getElementById("logoutMenuItem");
  const deleteSelectedButton = document.getElementById("deleteSelectedButton");

  if (token) {
    if (loginLink) {
      loginLink.style.display = "none";
    }
    if (logoutMenuItem) {
      logoutMenuItem.style.display = "block";
    }
    if (deleteSelectedButton) {
      deleteSelectedButton.classList.remove("disabled");
      deleteSelectedButton.removeAttribute("aria-disabled");
      deleteSelectedButton.removeAttribute("tabindex");
      deleteSelectedButton.style.pointerEvents = "auto";
      deleteSelectedButton.style.opacity = "1";
      // Восстанавливаем задний фон, как определён в ваших стилях (например, #2cccc4)
      deleteSelectedButton.style.backgroundColor = "#2cccc4";
      deleteSelectedButton.title = "";
    }
  } else {
    if (logoutMenuItem) {
      logoutMenuItem.style.display = "none";
    }
    if (deleteSelectedButton) {
      deleteSelectedButton.classList.add("disabled");
      deleteSelectedButton.setAttribute("aria-disabled", "true");
      deleteSelectedButton.setAttribute("tabindex", "-1");
      deleteSelectedButton.style.pointerEvents = "none";
      deleteSelectedButton.style.opacity = "0.5";
      // Сохраняем тот же базовый цвет фона, чтобы кнопка выглядела как у Каталога
      deleteSelectedButton.style.backgroundColor = "#2cccc4";
      deleteSelectedButton.style.color = "#ffffff";
      deleteSelectedButton.title = "Для удаления выбранных авторов необходимо войти в систему";
    }

    console.log("deleteSelectedButton backgroundColor:", deleteSelectedButton.style.backgroundColor);
  }

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
