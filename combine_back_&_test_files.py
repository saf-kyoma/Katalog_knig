import os


def combine_backend_w_test_files(output_file):
    """
    Combines the contents of all backend code files into a single output file.
    """
    # Директория с бэкенд-кодом
    backend_dir = 'bookstorage/src/main/java/org/application/bookstorage'

    # Директория с тест-кодом
    test_dir = 'bookstorage/src/test/java/org/application/bookstorage'

    # Поддерживаемые расширения файлов
    valid_extensions = ['.java', '.xml']

    # Сбор всех подходящих файлов
    files_to_combine = []
    
    for root, _, files in os.walk(backend_dir):
        for file in files:
            if any(file.endswith(ext) for ext in valid_extensions):
                files_to_combine.append(os.path.join(root, file))
                
    for root, _, files in os.walk(test_dir):
        for file in files:
            if any(file.endswith(ext) for ext in valid_extensions):
                files_to_combine.append(os.path.join(root, file))


    # Объединение файлов
    with open(output_file, 'w', encoding='utf-8') as outfile:
        for file_path in files_to_combine:
            if os.path.exists(file_path):
                try:
                    # Запись заголовка для каждого файла
                    outfile.write(f"\n\n===== {file_path} =====\n\n")
                    # Запись содержимого файла
                    with open(file_path, 'r', encoding='utf-8', errors='ignore') as infile:
                        outfile.write(infile.read())
                    print(f"Added: {file_path}")
                except Exception as e:
                    outfile.write(f"\n\n===== ERROR READING FILE: {file_path} =====\n\n")
                    outfile.write(str(e))
                    print(f"Error reading: {file_path}")
            else:
                print(f"File not found: {file_path}")


if __name__ == "__main__":
    # Выходной файл
    output_file = 'backfront_&_test_code.txt'

    # Выполнение функции сбора файлов
    combine_backend_w_test_files(output_file)

    # Вывод результата и ожидание завершения
    print(f"Backend and test files combined and saved to: {output_file}")
    input("Press Enter to exit...")
