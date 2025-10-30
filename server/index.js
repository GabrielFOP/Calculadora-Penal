const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const { addDays, format } = require('date-fns'); // opcional - usar somente se instalar date-fns

const app = express();
app.use(cors());
app.use(bodyParser.json());

// Helper simples (sem date-fns). Recebe Date e dias, retorna Date.
function addDaysSimple(date, days) {
  const d = new Date(date);
  d.setDate(d.getDate() + Math.round(days));
  return d;
}

function formatDateBR(date) {
  const d = new Date(date);
  const dd = String(d.getDate()).padStart(2, '0');
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const yyyy = d.getFullYear();
  return `${dd}/${mm}/${yyyy}`;
}

// Regras de negócio simples e documentadas
// - Convert pena para dias: anos*365 + meses*30 + dias
// - Remição: 1 dia a cada 3 dias trabalhados; 1 dia a cada 12 horas estudadas
// - Aggravantes: recidiva => +25% sobre necessidade (exige mais porcentagem), violencia => +10%
// - Percentuais base por tipo para progressão: (semi, open, condicional)
//   Comum: (30, 60, 70)
//   Hediondo / Org Crim (hediondo) / Tráfico: (50, 80, 100)
//   Hediondo com morte: (80, 100, 100)

app.post('/calculate', (req, res) => {
  try {
    const body = req.body;
    // Campos esperados:
    // totalYears, totalMonths, totalDays,
    // diasTrabalhados, horasEstudadas,
    // dataInicio (DD/MM/YYYY), crimeType, agravantes: { reincidente: bool, violencia: bool }, regimeInicial: 'Fechado'|'Semi-aberto'|'Aberto'

    const {
      totalYears = 0,
      totalMonths = 0,
      totalDays = 0,
      diasTrabalhados = 0,
      horasEstudadas = 0,
      dataInicio,
      crimeType,
      agravantes = {},
      regimeInicial = 'Fechado'
    } = body;

    if (!dataInicio || !crimeType) {
      return res.status(400).json({ error: 'Campos essenciais ausentes: dataInicio ou crimeType' });
    }

    // converter dataInicio DD/MM/YYYY para Date
    const parts = dataInicio.split('/');
    if (parts.length !== 3) return res.status(400).json({ error: 'Formato de data inválido' });
    const dia = parseInt(parts[0], 10);
    const mes = parseInt(parts[1], 10) - 1;
    const ano = parseInt(parts[2], 10);
    const startDate = new Date(ano, mes, dia);

    // converter pena total para dias
    const totalDaysSentence = (parseInt(totalYears) || 0) * 365 + (parseInt(totalMonths) || 0) * 30 + (parseInt(totalDays) || 0);

    // remição
    const remFromWork = Math.floor((parseInt(diasTrabalhados) || 0) / 3);
    const remFromStudy = Math.floor((parseInt(horasEstudadas) || 0) / 12);
    const totalRemicao = remFromWork + remFromStudy;

    // effective remaining days after remição
    let effectiveDays = Math.max(totalDaysSentence - totalRemicao, 0);

    // percentuais base por crime
    const crimeMap = {
      'Comum': { semi: 30, aberto: 60, cond: 70 },
      'Hediondo': { semi: 50, aberto: 80, cond: 100 },
      'Hediondo com resultado de morte': { semi: 80, aberto: 100, cond: 100 },
      'Organização criminosa (crime hediondo)': { semi: 50, aberto: 80, cond: 100 },
      'Tráfico de drogas': { semi: 50, aberto: 80, cond: 100 }
    };

    const base = crimeMap[crimeType] || crimeMap['Comum'];

    // aplicar agravantes (aumentam percentuais necessários)
    let adderPercent = 0;
    if (agravantes.reincidente) adderPercent += 25; // penaliza exigindo mais tempo
    if (agravantes.violencia) adderPercent += 10;

    const semiPercent = Math.min(base.semi + adderPercent, 100);
    const abertoPercent = Math.min(base.aberto + adderPercent, 100);
    const condPercent = Math.min(base.cond + adderPercent, 100);

    // ajustar conforme regime inicial: se já estiver em semi-aberto ou aberto, pulamos marcos anteriores
    const showSemi = regimeInicial === 'Fechado';
    const showAberto = regimeInicial !== 'Aberto'; // show aberto if closed or semi
    const showCond = true;

    // calcular datas (baseadas na fração da effectiveDays)
    function dateForPercent(pct) {
      const dias = Math.ceil(effectiveDays * (pct / 100.0));
      return addDaysSimple(startDate, dias);
    }

    const results = [];

    // Término da pena sem progressões e remições
    const terminoDate = addDaysSimple(startDate, effectiveDays);
    results.push({
      title: 'Término da Pena sem Progressões e Remições',
      date: formatDateBR(terminoDate),
      percent: '100.0% da pena'
    });

    if (showSemi) {
      const semDate = dateForPercent(semiPercent);
      results.push({
        title: 'Progressão para Regime Semiaberto',
        date: formatDateBR(semDate),
        percent: `${semiPercent.toFixed(1)}% da pena`
      });
    }

    if (showAberto) {
      const abDate = dateForPercent(abertoPercent);
      results.push({
        title: 'Progressão para Regime Aberto',
        date: formatDateBR(abDate),
        percent: `${abertoPercent.toFixed(1)}% da pena`
      });
    }

    // Liberdade condicional
    let condMsg = null;
    // condição simples: só se efetiveDays >= 730 (2 anos)
    if (effectiveDays < 730) {
      condMsg = 'Não há direito ao livramento condicional (pena inferior a 2 anos)';
    } else if (condPercent >= 100) {
      condMsg = 'Não há direito ao livramento condicional nas regras aplicadas (condicional exige 100%)';
    } else {
      const condDate = dateForPercent(condPercent);
      condMsg = `Data prevista: ${formatDateBR(condDate)} (${condPercent.toFixed(1)}% da pena)`;
    }

    return res.json({
      inputSummary: {
        totalDaysSentence,
        totalRemicao,
        effectiveDays,
        startDate: formatDateBR(startDate),
        crimeType,
        agravantes,
        regimeInicial
      },
      results,
      liberdadeCondicional: condMsg
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'erro interno' });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Servidor rodando em http://localhost:${PORT}`)); 
