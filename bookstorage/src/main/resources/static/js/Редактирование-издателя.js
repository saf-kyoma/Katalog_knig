// Функция для получения параметра из URL
  function getParameterByName(name) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(name);
  }

  // Функция для получения данных издательства по названию (ключ издательства)
  function fetchPublisherData(publisherName) {
    fetch(`/api/publishing-companies/${encodeURIComponent(publisherName)}`)
      .then(response => {
        if (!response.ok) {
          throw new Error('Издательство не найдено');
        }
        return response.json();
      })
      .then(publisher => {
        populateForm(publisher);
        // Записываем оригинальное название издательства в скрытое поле
        document.getElementById('originalName').value = publisher.name || '';
      })
      .catch(error => {
        console.error('Ошибка при загрузке данных издательства:', error);
        alert('Не удалось загрузить данные издательства.');
      });
  }

  // Функция для заполнения формы данными издательства
  function populateForm(publisher) {
    document.getElementById('name').value = publisher.name || '';
    // Если дата в формате ISO (например, "2000-01-01"), берём первые 4 символа
    document.getElementById('year').value = publisher.establishmentYear ? publisher.establishmentYear.substring(0, 4) : '';
    document.getElementById('contact').value = publisher.contactInfo || '';
    document.getElementById('city').value = publisher.city || '';
  }

  // Обработка отправки формы редактирования издательства
  document.getElementById('publisher-edit-form').addEventListener('submit', function(event) {
    event.preventDefault();
    // Берём оригинальное название из скрытого поля
    const originalName = document.getElementById('originalName').value;
    if (!originalName) {
      alert('Оригинальное название издательства не указано.');
      return;
    }
    // Собираем данные из формы
    const publisherData = {
      name: document.getElementById('name').value.trim(),
      contactInfo: document.getElementById('contact').value.trim() || null,
      city: document.getElementById('city').value.trim() || null
    };
    // Для поля года: если введено 4 цифры, дополнить до формата LocalDate ("YYYY-01-01")
    const yearValue = document.getElementById('year').value.trim();
    if (yearValue.length === 4) {
      publisherData.establishmentYear = yearValue + "-01-01";
    } else {
      publisherData.establishmentYear = yearValue || null;
    }
    // Отправляем PUT-запрос с использованием оригинального названия в URL
    fetch(`/api/publishing-companies/${encodeURIComponent(originalName)}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(publisherData)
    })
    .then(response => {
      if (!response.ok) {
        return response.text().then(text => { throw new Error(text); });
      }
      return response.json();
    })
    .then(data => {
      const successMsg = document.querySelector('.u-form-send-success');
      successMsg.style.display = 'block';
      setTimeout(() => {
        successMsg.style.display = 'none';
      }, 3000);
      // Если название изменилось, обновляем скрытое поле оригинального имени
      document.getElementById('originalName').value = data.name || '';
    })
    .catch(error => {
      console.error('Ошибка:', error);
      const errorMsg = document.querySelector('.u-form-send-error');
      errorMsg.textContent = `Отправка не удалась: ${error.message}`;
      errorMsg.style.display = 'block';
      setTimeout(() => {
        errorMsg.style.display = 'none';
      }, 5000);
    });
  });

  // При загрузке страницы извлекаем название издательства из URL и запрашиваем его данные
  document.addEventListener('DOMContentLoaded', function() {
    const publisherName = getParameterByName('name');
    if (!publisherName) {
      alert('Название издательства не указано в URL.');
      return;
    }
    fetchPublisherData(publisherName);
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