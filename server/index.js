const express = require("express");
const bodyParser = require("body-parser");
const cors = require("cors");

const app = express();
app.use(cors());
app.use(bodyParser.json());


function addDays(date, days) {
  const d = new Date(date);
  d.setDate(d.getDate() + Math.round(days));
  return d;
}

function formatDateBR(date) {
  const d = new Date(date);
  const dd = String(d.getDate()).padStart(2, "0");
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const yyyy = d.getFullYear();
  return `${dd}/${mm}/${yyyy}`;
}


app.post("/calculate", (req, res) => {
  try {
    const {
      totalYears = 0,
      totalMonths = 0,
      totalDays = 0,
      diasTrabalhados = 0,
      horasEstudadas = 0,
      dataInicio,
      crimeType,
      agravantes = {},
      regimeInicial = "Fechado",
    } = req.body;

    if (!dataInicio || !crimeType) {
      return res.status(400).json({
        error: "Campos obrigatórios ausentes: dataInicio e crimeType",
      });
    }

    
    let startDate;
    if (dataInicio.includes("/")) {
      const [dia, mes, ano] = dataInicio.split("/").map((p) => parseInt(p, 10));
      startDate = new Date(ano, mes - 1, dia);
    } else {
      startDate = new Date(dataInicio);
    }

    if (isNaN(startDate.getTime())) {
      return res.status(400).json({ error: "Data de início inválida." });
    }

    
    const totalDaysSentence =
      (parseInt(totalYears) || 0) * 365 +
      (parseInt(totalMonths) || 0) * 30 +
      (parseInt(totalDays) || 0);

    
    const remFromWork = Math.floor((parseInt(diasTrabalhados) || 0) / 3);
    const remFromStudy = Math.floor((parseInt(horasEstudadas) || 0) / 12);
    const totalRemicao = remFromWork + remFromStudy;

    const effectiveDays = Math.max(totalDaysSentence - totalRemicao, 0);

   
    const reincidente = !!agravantes.reincidente;
    const violencia = !!agravantes.violencia;

   
    let semiPercent = 0;
    let abertoPercent = 0;

    switch (crimeType) {
      case "Comum":
        if (violencia) {
          semiPercent = reincidente ? 30 : 25;
        } else {
          semiPercent = reincidente ? 20 : 16;
        }
        abertoPercent = semiPercent * 2; // aberto = o dobro
        break;

      case "Hediondo":
      case "Tráfico de drogas":
      case "Organização criminosa (crime hediondo)":
        semiPercent = reincidente ? 60 : 40;
        abertoPercent = semiPercent * 2 <= 100 ? semiPercent * 2 : 80;
        break;

      case "Hediondo com resultado de morte":
        semiPercent = reincidente ? 70 : 50;
        abertoPercent = semiPercent * 2 <= 100 ? semiPercent * 2 : 100;
        break;

      default:
        semiPercent = 16;
        abertoPercent = 32;
        break;
    }

    if (abertoPercent > 100) abertoPercent = 100;

    
    const dateForPercent = (pct) => {
      const dias = Math.max(
        totalDaysSentence * (pct / 100) - totalRemicao,
        0
      );
      return addDays(startDate, dias);
    };

 
    const semiDate = dateForPercent(semiPercent);
    const abertoDate = dateForPercent(abertoPercent);
    const terminoDate = addDays(startDate, effectiveDays);


    let condMsg;
    if (effectiveDays < 730) {
      condMsg =
        "Não há direito ao livramento condicional (pena inferior a 2 anos).";
    }
    else if(crimeType == "Hediondo com resultado de morte"){
        condMsg = 
        "Não há direito ao livramento condicional (crime hediondo com resultado morte)"
    }
    else if(reincidente && crimeType == "Hediondo" || "Organização criminosa (crime hediondo)" || "Tráfico de drogas"){
        condMsg = 
        "Não há direito ao livramento condicional (reincidente em crime hediondo)";
    }
    else {
      const condPercent = reincidente ? 50 : 33;
      const condDate = dateForPercent(condPercent);
      condMsg = `Data prevista: ${formatDateBR(
        condDate
      )} (${condPercent}% da pena total)`;
    }


    const results = [];

    if (regimeInicial === "Fechado") {
      results.push({
        title: "Progressão para Regime Semiaberto",
        date: formatDateBR(semiDate),
        percent: `${semiPercent}% da pena total`,
      });
      results.push({
        title: "Progressão para Regime Aberto",
        date: formatDateBR(abertoDate),
        percent: `${abertoPercent}% da pena total`,
      });
    } else if (regimeInicial === "Semi-aberto") {
      results.push({
        title: "Progressão para Regime Aberto",
        date: formatDateBR(abertoDate),
        percent: `${abertoPercent}% da pena total`,
      });
    }

    results.push({
      title: "Término da Pena (após remições)",
      date: formatDateBR(terminoDate),
      percent: "100% da pena",
    });


    res.json({
      inputSummary: {
        totalDaysSentence,
        totalRemicao,
        effectiveDays,
        startDate: formatDateBR(startDate),
        crimeType,
        agravantes,
        regimeInicial,
      },
      results,
      liberdadeCondicional: condMsg,
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Erro interno no servidor." });
  }
});


const PORT = 3000;
app.listen(PORT, "0.0.0.0", () =>
  console.log(`Servidor rodando em http://0.0.0.0:${PORT}`)
);
