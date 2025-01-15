async function handleLogin(event) {
          event.preventDefault(); // предотвратить стандартную отправку формы
          const username = document.getElementById('username').value;
          const password = document.getElementById('password').value;

          try {
            const response = await fetch('/api/auth/login', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ username, password })
            });

            if (response.ok) {
              const data = await response.json();
              // Сохраняем полученный JWT-токен (например, в localStorage)
              localStorage.setItem('jwtToken', data.token);
              alert("Успешный вход!");
              window.location.href = 'Каталог.html';  // перенаправление на каталог
            } else {
              alert("Неверное имя пользователя или пароль.");
            }
          } catch (error) {
            console.error("Ошибка подключения:", error);
            alert("Ошибка подключения к серверу.");
          }
        }