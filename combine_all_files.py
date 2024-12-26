import os


def combine_files(output_file):
    """
    Combines the contents of all backend and frontend code files into a single output file.
    """
    # Directories to include
    backend_dir = 'bookstorage/src/main/java/org/application/bookstorage'
    frontend_dir = 'bookstorage/src/main/resources/static'

    # Supported file extensions
    valid_extensions = ['.java', '.xml', '.html', '.css', '.js']

    # Collect all valid files
    files_to_combine = []

    # Add backend files
    for root, _, files in os.walk(backend_dir):
        for file in files:
            if any(file.endswith(ext) for ext in valid_extensions):
                files_to_combine.append(os.path.join(root, file))

    # Add frontend files
    for root, _, files in os.walk(frontend_dir):
        for file in files:
            if any(file.endswith(ext) for ext in valid_extensions):
                files_to_combine.append(os.path.join(root, file))

    # Combine files
    with open(output_file, 'w', encoding='utf-8') as outfile:
        for file_path in files_to_combine:
            if os.path.exists(file_path):
                try:
                    # Write header for each file
                    outfile.write(f"\n\n===== {file_path} =====\n\n")
                    # Write content of the file
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
    # Output file
    output_file = 'all_code.txt'

    # Execute the file combination function
    combine_files(output_file)

    # Print the output file location and wait for user input
    print(f"Files combined and saved to: {output_file}")
    input("Press Enter to exit...")
