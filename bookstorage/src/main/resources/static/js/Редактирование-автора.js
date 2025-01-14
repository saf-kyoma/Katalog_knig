// Функция для получения параметра из URL
    function getParameterByName(name) {
      const urlParams = new URLSearchParams(window.location.search);
      return urlParams.get(name);
    }

    // Функция для получения данных автора по ID и заполнения формы
    function fetchAuthorData(authorId) {
      fetch(`/api/authors/${encodeURIComponent(authorId)}`)
        .then(response => {
          if (!response.ok) {
            throw new Error('Автор не найден');
          }
          return response.json();
        })
        .then(author => {
          populateForm(author);
        })
        .catch(error => {
          console.error('Ошибка при загрузке данных автора:', error);
          alert('Не удалось загрузить данные автора.');
        });
    }

    // Функция для заполнения формы данными автора
    function populateForm(author) {
      document.getElementById('name').value = author.fio || '';
      document.getElementById('publicationYear').value = author.birthDate || '';
      document.getElementById('contry').value = author.country || '';
      document.getElementById('Nickname').value = author.nickname || '';
    }

    // Обработка отправки формы редактирования
    document.getElementById('author-edit-form').addEventListener('submit', function(event) {
      event.preventDefault();
      const authorId = getParameterByName('id');
      if (!authorId) {
        alert('ID автора не указан.');
        return;
      }
      const authorData = {
        id: authorId,
        fio: document.getElementById('name').value.trim(),
        birthDate: document.getElementById('publicationYear').value,
        country: document.getElementById('contry').value.trim(),
        nickname: document.getElementById('Nickname').value.trim()
      };
      fetch(`/api/authors/${encodeURIComponent(authorId)}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(authorData)
      })
      .then(response => {
        if (!response.ok) {
          throw new Error('Ошибка при обновлении автора');
        }
        return response.json();
      })
      .then(data => {
        const successMsg = document.querySelector('.u-form-send-success');
        successMsg.style.display = 'block';
        setTimeout(() => {
          successMsg.style.display = 'none';
        }, 3000);
      })
      .catch(error => {
        console.error('Ошибка:', error);
        const errorMsg = document.querySelector('.u-form-send-error');
        errorMsg.style.display = 'block';
        setTimeout(() => {
          errorMsg.style.display = 'none';
        }, 3000);
      });
    });

    // При загрузке страницы извлекаем ID автора из URL и запрашиваем его данные
    document.addEventListener('DOMContentLoaded', function() {
      const authorId = getParameterByName('id');
      if (!authorId) {
        alert('ID автора не указан в URL.');
        return;
      }
      fetchAuthorData(authorId);
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