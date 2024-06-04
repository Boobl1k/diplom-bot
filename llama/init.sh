echo start

/bin/ollama serve & # запуск ollama

if [ -f "/model/llama3-tuned.gguf" ]; then
  echo "model already downloaded"
else
  apt-get install -y curl
  # скачивание модели
  curl -o /model/llama3-tuned.gguf \
   -L https://huggingface.co/mansooooor/llama3-fine-tuned-gguf/resolve/main/llama3-fine-tuned-gguf-unsloth.Q4_K_M.gguf?download=true
fi

sleep 5
# добавление модели в ollama и настройка с помощью Modelfile
ollama create llama3-tuned -f /llama/Modelfile
ollama list

tail -f /dev/null
