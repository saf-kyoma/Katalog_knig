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

  // При нажатии на "Выгрузить базу данных" (export)
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

// *** ДОБАВЛЕННЫЙ КОД ДЛЯ АВТОРИЗАЦИИ И БЛОКИРОВКИ КНОПОК (Редактировать, Отмена) ***
document.addEventListener("DOMContentLoaded", function () {
  // Получаем токен из localStorage (токен сохраняется после успешного входа под ключом "jwtToken")
  const token = localStorage.getItem("jwtToken");

  // Получаем элементы для навигации
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
    // Блокируем кнопки и выполняем редирект
    const editAuthorSubmitBtn = document.getElementById("editAuthorSubmitBtn");
    const cancelButton = document.getElementById("cancelButton");

    if (editAuthorSubmitBtn) {
      editAuthorSubmitBtn.classList.add("disabled");
      editAuthorSubmitBtn.setAttribute("aria-disabled", "true");
      editAuthorSubmitBtn.style.pointerEvents = "none";
      editAuthorSubmitBtn.style.opacity = "0.5";
      editAuthorSubmitBtn.title = "Для редактирования автора необходимо войти в систему";
      editAuthorSubmitBtn.style.backgroundColor = "#2cccc4";
      editAuthorSubmitBtn.style.color = "#ffffff";
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
    alert("Для доступа к странице редактирования автора необходимо войти в систему.");
    window.location.href = "Вход-в-систему.html";
    return; // Прерываем дальнейшее выполнение
  }

  // Если токен есть — разблокируем кнопки (на случай, если ранее были заблокированы)
  const editAuthorSubmitBtn = document.getElementById("editAuthorSubmitBtn");
  const cancelButton = document.getElementById("cancelButton");
  if (editAuthorSubmitBtn) {
    editAuthorSubmitBtn.classList.remove("disabled");
    editAuthorSubmitBtn.removeAttribute("aria-disabled");
    editAuthorSubmitBtn.style.pointerEvents = "auto";
    editAuthorSubmitBtn.style.opacity = "1";
    editAuthorSubmitBtn.title = "";
    editAuthorSubmitBtn.style.backgroundColor = "";
    editAuthorSubmitBtn.style.color = "";
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
