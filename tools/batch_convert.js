const fs = require('fs');
const path = require('path');
const s2v = require('svg2vectordrawable');

async function convertAll() {
  const inDir = path.join(__dirname, 'svg_repo');
  const outDir = path.join(__dirname, 'xml_converted');
  if (!fs.existsSync(outDir)) {
    fs.mkdirSync(outDir, { recursive: true });
  }

  const files = fs.readdirSync(inDir).filter(f => f.endsWith('.svg'));
  console.log(`Converting ${files.length} SVGs to Android VectorDrawables...`);

  let successCount = 0;
  let failCount = 0;

  for (const f of files) {
    const inPath = path.join(inDir, f);
    const outName = f.replace('.svg', '.xml');
    const outPath = path.join(outDir, outName);

    let content = fs.readFileSync(inPath, 'utf8');

    // Clean up SVG features unsupported by Android VectorDrawable
    content = content.replace(/<symbol[\s\S]*?<\/symbol>/gi, '');
    content = content.replace(/<mask[\s\S]*?<\/mask>/gi, '');
    content = content.replace(/<mask[^>]*\/>/gi, '');
    content = content.replace(/mask="[^"]*"/gi, '');
    content = content.replace(/filter="[^"]*"/gi, '');
    content = content.replace(/<filter[\s\S]*?<\/filter>/gi, '');

    try {
      const xml = await s2v(content, {
        floatPrecision: 2,
        xmlTag: false
      });
      fs.writeFileSync(outPath, xml, 'utf8');
      successCount++;
    } catch (err) {
      console.error(`Failed ${f}: ${err.message}`);
      failCount++;
    }
  }

  console.log(`Conversion completed: ${successCount} succeeded, ${failCount} failed.`);
}

convertAll();

