
# Autor
Nome: Gabriel Fernandes de Olivera
RA: 24.01414-0

---

# Calculadora Penal

Aplicação completa para **cálculo automatizado de progressões de pena** e **benefícios penais**, desenvolvida em **Kotlin (Jetpack Compose)** no frontend e **Node.js (Express)** no backend.  
A ferramenta tem como objetivo auxiliar advogados e operadores do Direito na análise rápida de progressões e remições.

---

## Tecnologias Utilizadas

### Frontend
- **Kotlin**
- **Jetpack Compose** (UI declarativa)
- **Retrofit** (requisições HTTP)
- **Material Design 3**
- **Coroutines** (para chamadas assíncronas)
- **Android Studio**

### Backend
- **Node.js**
- **Express.js**
- **CORS**
- **Body-parser**
- **JavaScript ES6**

---

## Funcionalidades

- Cálculo automático da **data de progressão de regime** (fechado → semiaberto → aberto)
- Cálculo do **término da pena** considerando **remições por trabalho e estudo**
- Exibição da **data de livramento condicional**
- Interface intuitiva e responsiva em Jetpack Compose
- Opção de **contatar advogado via WhatsApp** diretamente no app

---

## Manual do Usuário

### Iniciando o aplicativo

1. Abra o app **Calculadora Penal** no seu dispositivo Android.
2. Na tela inicial, toque em **"Calcular Pena"**.
3. Preencha as informações solicitadas:
   - **Tempo total da pena** (anos, meses, dias)
   - **Remição por trabalho e estudo**
   - **Data de início do cumprimento da pena**
   - **Tipo de crime**
   - **Agravantes** (reincidência, violência ou grave ameaça)
   - **Regime inicial**

4. Toque em **Calcular**.
5. O app exibirá:
   - Datas previstas de progressão
   - Percentuais cumpridos da pena
   - Data de término
   - Data prevista para livramento condicional

---

## Lógica de Cálculo da Pena

A aplicação segue as regras previstas na **Lei de Execução Penal (LEP)** e jurisprudência consolidada, conforme os percentuais abaixo:

| Situação | Percentual para Progressão |
|-----------|----------------------------|
| Crime comum | 16% (semiaberto), 32% (aberto) |
| Crime comum reincidente | 20% (semiaberto), 40% (aberto) |
| Com violência ou grave ameaça | 25% (semiaberto), 50% (aberto) |
| Com violência e reincidente | 30% (semiaberto), 60% (aberto) |
| Hediondo | 40% (semiaberto), 80% (aberto) |
| Hediondo reincidente | 60% (semiaberto), 100% (aberto) |
| Hediondo com resultado morte | 50% (semiaberto), 100% (aberto) |
| Hediondo com resultado morte reincidente | 70% (semiaberto), 100% (aberto) |

Além disso, o cálculo considera:
- **1 dia de pena remido a cada 3 dias trabalhados**
- **1 dia de pena remido a cada 12 horas de estudo**

---

## Comunicação entre App e Servidor

- O app se comunica via **HTTP** com o backend Node.js.
- Para funcionar em **dispositivos reais**, o servidor deve ser iniciado ouvindo em:
  ```js
  app.listen(3000, "0.0.0.0");
  ```
- No Android, o IP configurado no Retrofit deve ser o IP local da máquina onde o Node.js está rodando:
  ```kt
  .baseUrl("http://SEU_IP_LOCAL:3000/")
  ```
---

## Como Executar o Projeto

### BackEnd
 ```js
 # Instalar dependências
 npm install

 # Rodar servidor
 node index.js
  ```
### Frontend (Android)

1. Abra o projeto no Android Studio.
2. Conecte um emulador ou dispositivo físico.
3. Ajuste o IP no MainActivity.kt:
    ```kt
    .baseUrl("http://SEU_IP_LOCAL:3000/")
    ```
4. Ajuste o IP no AndroidManifest
5. Execute o App
