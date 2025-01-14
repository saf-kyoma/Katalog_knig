 // Функция для получения параметра из URL
    function getQueryParam(param) {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get(param);
    }

    // Функция для отображения сообщения
    function showMessage(message, isError = true) {
        const messageDiv = document.getElementById('message');
        messageDiv.textContent = message;
        messageDiv.style.color = isError ? 'red' : 'green';
    }

    // Получаем ISBN из URL
    const isbn = getQueryParam('isbn');

    if (!isbn) {
        showMessage('ISBN книги не указан в URL.');
    } else {
        // Запрос к API для получения информации о книге
        fetch(`/api/books/${encodeURIComponent(isbn)}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Книга не найдена.');
                }
                return response.json();
            })
            .then(data => {
                // Заполняем таблицу данными
                document.getElementById('isbn').textContent = data.isbn;
                document.getElementById('name').textContent = data.name;
                document.getElementById('publicationYear').textContent = data.publicationYear;
                document.getElementById('ageLimit').textContent = data.ageLimit;
                document.getElementById('publishingCompany').textContent = data.publishingCompany;
                document.getElementById('pageCount').textContent = data.pageCount;
                document.getElementById('language').textContent = data.language;
                document.getElementById('cost').textContent = data.cost;
                document.getElementById('countOfBooks').textContent = data.countOfBooks;

                // Обработка авторов с проверкой на наличие псевдонима
                const authors = data.authors.map(author => {
                    if (author.nickname && author.nickname.trim() !== '') {
                        return `${author.fio} (${author.nickname})`;
                    } else {
                        return author.fio;
                    }
                }).join(', ');
                document.getElementById('authors').textContent = authors;

                // Обработка жанров
                const genres = data.genres.join(', ');
                document.getElementById('genres').textContent = genres;

                // Настройка кнопки редактирования
                const editButton = document.getElementById('editButton');
                editButton.href = `Редактирование-книги.html?isbn=${encodeURIComponent(data.isbn)}`;
            })
            .catch(error => {
                showMessage(error.message);
            });

        // Обработчик кнопки удаления
        document.getElementById('deleteButton').addEventListener('click', () => {
            if (confirm('Вы уверены, что хотите удалить эту книгу?')) {
                fetch(`/api/books/${encodeURIComponent(isbn)}`, {
                    method: 'DELETE'
                })
                .then(response => {
                    if (response.status === 204) {
                        alert('Книга успешно удалена.');
                        window.location.href = 'catalog.html';
                    } else {
                        throw new Error('Ошибка при удалении книги.');
                    }
                })
                .catch(error => {
                    showMessage(error.message);
                });
            }
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