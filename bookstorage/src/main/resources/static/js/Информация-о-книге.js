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

// *** ДОБАВЛЕННЫЙ КОД ДЛЯ АВТОРИЗАЦИИ И БЛОКИРОВКИ КНОПОК РЕДАКТИРОВАНИЯ, УДАЛЕНИЯ *** 
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

    // Блокировка кнопок "Редактировать" и "Удалить" для неавторизованного пользователя
    const editButton = document.getElementById("editButton");
    const deleteButton = document.getElementById("deleteButton");

    if (!token) {
        if (editButton) {
            editButton.classList.add("disabled");
            editButton.setAttribute("aria-disabled", "true");
            editButton.style.pointerEvents = "none";
            editButton.style.opacity = "0.5";
            editButton.title = "Для редактирования книги необходимо войти в систему";
            editButton.style.backgroundColor = "#2cccc4";
            editButton.style.color = "#ffffff";
        }
        if (deleteButton) {
            deleteButton.classList.add("disabled");
            deleteButton.setAttribute("aria-disabled", "true");
            deleteButton.style.pointerEvents = "none";
            deleteButton.style.opacity = "0.5";
            deleteButton.title = "Для удаления книги необходимо войти в систему";
            deleteButton.style.backgroundColor = "#2cccc4";
            deleteButton.style.color = "#ffffff";
        }
    } else {
        if (editButton) {
            editButton.classList.remove("disabled");
            editButton.removeAttribute("aria-disabled");
            editButton.style.pointerEvents = "auto";
            editButton.style.opacity = "1";
            editButton.title = "";
            // Можно сбросить стили фона и цвета, если требуются
            editButton.style.backgroundColor = "";
            editButton.style.color = "";
        }
        if (deleteButton) {
            deleteButton.classList.remove("disabled");
            deleteButton.removeAttribute("aria-disabled");
            deleteButton.style.pointerEvents = "auto";
            deleteButton.style.opacity = "1";
            deleteButton.title = "";
            deleteButton.style.backgroundColor = "";
            deleteButton.style.color = "";
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

