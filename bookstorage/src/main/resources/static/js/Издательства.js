let publishingCompanies = [];
let currentSortColumn = '';
let currentSortOrder = 'asc';

// Функция для загрузки всех издательств при загрузке страницы
document.addEventListener('DOMContentLoaded', () => {
  fetchPublishingCompanies();
  initializeEventListeners();
});

// Функция для получения всех издательств
function fetchPublishingCompanies() {
  fetch('/api/publishing-companies')
    .then(response => {
      if (!response.ok) {
        throw new Error('Ошибка при загрузке издательств');
      }
      return response.json();
    })
    .then(data => {
      publishingCompanies = data;
      renderTable(publishingCompanies);
    })
    .catch(error => {
      console.error('Ошибка:', error);
      alert('Не удалось загрузить издательства. Пожалуйста, попробуйте позже.');
    });
}

// Функция для отображения издательств в таблице
function renderTable(companies) {
  const tableBody = document.getElementById('publishing-companies-table-body');
  tableBody.innerHTML = '';

  companies.forEach(company => {
    const row = document.createElement('tr');

    // Чекбокс для выбора
    const checkboxCell = document.createElement('td');
    checkboxCell.classList.add('checkbox-column');
    const checkbox = document.createElement('input');
    checkbox.type = 'checkbox';
    checkbox.dataset.name = company.name;
    checkboxCell.appendChild(checkbox);
    row.appendChild(checkboxCell);

    // Название (гиперссылка)
    const nameCell = document.createElement('td');
    const link = document.createElement('a');
    link.href = `Информация-об-издательстве.html?name=${encodeURIComponent(company.name)}`;
    link.textContent = company.name || 'Без названия';
    link.className = 'text-decoration-none';
    link.style.color = '#0d6efd';
    nameCell.appendChild(link);
    row.appendChild(nameCell);

    // Год основания
    const yearCell = document.createElement('td');
    yearCell.textContent = company.establishmentYear || 'Не указано';
    row.appendChild(yearCell);

    // Контактная информация
    const contactCell = document.createElement('td');
    contactCell.textContent = company.contactInfo || 'Не указано';
    row.appendChild(contactCell);

    // Город
    const cityCell = document.createElement('td');
    cityCell.textContent = company.city || 'Не указано';
    row.appendChild(cityCell);

    tableBody.appendChild(row);
  });
}

// Функция для обработки поиска
function handleSearch() {
  const query = document.getElementById('searchInput').value.trim();
  if (query === '') {
    fetchPublishingCompanies();
    resetSortArrows();
    return;
  }

  fetch(`/api/publishing-companies/search?q=${encodeURIComponent(query)}`)
    .then(response => {
      if (!response.ok) {
        throw new Error('Ошибка при поиске издательств');
      }
      return response.json();
    })
    .then(data => {
      publishingCompanies = data;
      renderTable(publishingCompanies);
      resetSortArrows();
    })
    .catch(error => {
      console.error('Ошибка:', error);
      alert('Не удалось выполнить поиск. Пожалуйста, попробуйте позже.');
    });
}

// Функция для сброса поиска и отображения всех издательств
function resetSearch() {
  document.getElementById('searchInput').value = '';
  fetchPublishingCompanies();
  resetSortArrows();
}

// Функция для сортировки таблицы
function sortTable(column) {
  if (currentSortColumn === column) {
    currentSortOrder = currentSortOrder === 'asc' ? 'desc' : 'asc';
  } else {
    currentSortColumn = column;
    currentSortOrder = 'asc';
  }

  publishingCompanies.sort((a, b) => {
    let valA = a[column];
    let valB = b[column];

    if (valA === undefined || valA === null) valA = '';
    if (valB === undefined || valB === null) valB = '';

    if (typeof valA === 'string') valA = valA.toLowerCase();
    if (typeof valB === 'string') valB = valB.toLowerCase();

    if (valA < valB) return currentSortOrder === 'asc' ? -1 : 1;
    if (valA > valB) return currentSortOrder === 'asc' ? 1 : -1;
    return 0;
  });

  updateSortArrows();
  renderTable(publishingCompanies);
}

// Функция для обновления стрелочек сортировки
function updateSortArrows() {
  const buttons = document.querySelectorAll('.sort-button');
  buttons.forEach(button => {
    const column = button.getAttribute('data-column');
    if (column === currentSortColumn) {
      button.textContent = currentSortOrder === 'asc' ? '▲' : '▼';
      button.classList.remove('btn-light');
      button.classList.add('btn-secondary');
    } else {
      button.textContent = '▲';
      button.classList.remove('btn-secondary');
      button.classList.add('btn-light');
    }
  });
}

// Функция для сброса стрелочек сортировки
function resetSortArrows() {
  currentSortColumn = '';
  currentSortOrder = 'asc';
  const buttons = document.querySelectorAll('.sort-button');
  buttons.forEach(button => {
    button.textContent = '▲';
    button.classList.remove('btn-secondary');
    button.classList.add('btn-light');
  });
}

// Функция для выбора/снятия всех чекбоксов
function toggleAllCheckboxes(source) {
  const checkboxes = document.querySelectorAll('tbody input[type="checkbox"]');
  checkboxes.forEach(checkbox => checkbox.checked = source.checked);
}

// Функция для удаления выбранных издательств
function deleteSelected() {
  const checkboxes = document.querySelectorAll('#publishing-companies-table-body input[type="checkbox"]:checked');
  if (checkboxes.length === 0) {
    alert('Пожалуйста, выберите хотя бы одно издательство для удаления.');
    return;
  }

  const confirmed = confirm('Вы уверены, что хотите удалить выбранные издательства?\nВсе книги данных издательств тоже будут удалены!');
  if (!confirmed) {
    return;
  }

  const names = Array.from(checkboxes).map(ch => ch.dataset.name);

  fetch('/api/publishing-companies/bulk-delete', {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(names)
  })
  .then(response => {
    if (response.status === 204) {
      alert('Выбранные издательства (и их книги) успешно удалены.');
      fetchPublishingCompanies();
    } else {
      alert('Не удалось удалить издательства. Возможно, некоторые не найдены.');
    }
  })
  .catch(error => {
    console.error('Ошибка при удалении издательств:', error);
    alert('Произошла ошибка при удалении издательств. Подробности в консоли.');
  });
}

// Инициализация обработчиков событий
function initializeEventListeners() {
  const searchForm = document.getElementById('searchForm');
  if (searchForm) {
    searchForm.addEventListener('submit', handleSearch);
  }

  const searchButton = document.getElementById('searchButton');
  if (searchButton) {
    searchButton.addEventListener('click', handleSearch);
  }

  const resetButton = document.getElementById('resetButton');
  if (resetButton) {
    resetButton.addEventListener('click', resetSearch);
  }

  const sortButtonsElements = document.querySelectorAll('.sort-button');
  sortButtonsElements.forEach(button => {
    const columnName = button.getAttribute('data-column');
    button.addEventListener('click', () => sortTable(columnName));
  });
}

// *** ДОБАВЛЕННЫЙ КОД ДЛЯ КНОПОК ИМПОРТА/ЭКСПОРТА ***
document.addEventListener('DOMContentLoaded', function() {
  const importBtn = document.getElementById('DBadd');
  const exportBtn = document.getElementById('DBout');

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

// *** ДОБАВЛЕННЫЙ КОД ДЛЯ АВТОРИЗАЦИИ ***
document.addEventListener("DOMContentLoaded", function () {
  // Получаем токен из localStorage (токен сохраняется после успешного входа под ключом "jwtToken")
  const token = localStorage.getItem("jwtToken");

  // Получаем элементы по id
  const loginLink = document.getElementById("loginLink");
  const logoutMenuItem = document.getElementById("logoutMenuItem");

  if (token) {
    // Если токен найден – пользователь авторизован, скрываем ссылку "Войти" и показываем "Выход"
    if (loginLink) {
      loginLink.style.display = "none";
    }
    if (logoutMenuItem) {
      logoutMenuItem.style.display = "block";
    }
  } else {
    // Если токена нет – пользователь не авторизован, скрываем меню "Выход"
    if (logoutMenuItem) {
      logoutMenuItem.style.display = "none";
    }
  }

  // Блокировка кнопки "Удалить выбранные" для неавторизованного пользователя
  const deleteSelectedButton = document.querySelector("a[onclick='deleteSelected()']");
  if (!token) {
    if (deleteSelectedButton) {
     deleteSelectedButton.classList.add("disabled");
                deleteSelectedButton.setAttribute("aria-disabled", "true");
                deleteSelectedButton.setAttribute("tabindex", "-1");
                deleteSelectedButton.style.pointerEvents = "none";
                deleteSelectedButton.style.opacity = "0.5";
                // Сохраняем тот же базовый цвет фона, чтобы кнопка выглядела как у Каталога
                deleteSelectedButton.style.backgroundColor = "#2cccc4";
                deleteSelectedButton.style.color = "#ffffff";


    }

  } else {
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

