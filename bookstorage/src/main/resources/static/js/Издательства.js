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
      link.style.color = '#0d6efd'; // Цвет ссылки
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
      // Если уже сортируем по этому столбцу, то переключаем порядок
      currentSortOrder = currentSortOrder === 'asc' ? 'desc' : 'asc';
    } else {
      // Иначе, сортируем по новому столбцу, в порядке возрастания
      currentSortColumn = column;
      currentSortOrder = 'asc';
    }

    // Сортируем массив
    publishingCompanies.sort((a, b) => {
      let valA = a[column];
      let valB = b[column];

      // Обработка отсутствующих значений
      if (valA === undefined || valA === null) valA = '';
      if (valB === undefined || valB === null) valB = '';

      // Преобразуем строки в нижний регистр для нечувствительной сортировки
      if (typeof valA === 'string') valA = valA.toLowerCase();
      if (typeof valB === 'string') valB = valB.toLowerCase();

      if (valA < valB) return currentSortOrder === 'asc' ? -1 : 1;
      if (valA > valB) return currentSortOrder === 'asc' ? 1 : -1;
      return 0;
    });

    // Обновляем стрелочки
    updateSortArrows();

    // Рендерим таблицу
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
    // Получаем все выбранные чекбоксы
    const checkboxes = document.querySelectorAll('#publishing-companies-table-body input[type="checkbox"]:checked');
    if (checkboxes.length === 0) {
      alert('Пожалуйста, выберите хотя бы одно издательство для удаления.');
      return;
    }

    // Спросим подтверждение у пользователя
    const confirmed = confirm('Вы уверены, что хотите удалить выбранные издательства?\nВсе книги данных издательств тоже будут удалены!');
    if (!confirmed) {
      // Если пользователь нажал "Отмена", ничего не делаем
      return;
    }

    // Собираем массив названий издательств
    const names = Array.from(checkboxes).map(ch => ch.dataset.name);

    // Отправляем DELETE-запрос на наш новый эндпоинт /api/publishing-companies/bulk-delete
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
        // Перезагружаем таблицу (или страницу), чтобы изменения отобразились
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
    // Обработчик отправки формы поиска
    const searchForm = document.getElementById('searchForm');
    if (searchForm) {
      searchForm.addEventListener('submit', handleSearch);
    }

    // Обработчик кнопки "Искать"
    const searchButton = document.getElementById('searchButton');
    if (searchButton) {
      searchButton.addEventListener('click', handleSearch);
    }

    // Обработчик кнопки "Сбросить поиск"
    const resetButton = document.getElementById('resetButton');
    if (resetButton) {
      resetButton.addEventListener('click', resetSearch);
    }

    // Обработчики сортировки
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