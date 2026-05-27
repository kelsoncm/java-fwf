package com.kelsoncm.libs.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.Adler32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * Utilitário para trabalhar com compactação e descompactação de arquivos
 *
 * @author edle.silva
 *
 */
public final class ZipUtils {

    private static final int TAMANHO_BUFFER = 1024;
    private static final String STRING_EMPTY = "";
    /**
     * Filtro para arquivos ZIP (*.ZIP)
     */
    public static final FileFilter FILE_FILTER_ZIP = new FileFilter() {
        public boolean accept(final File pathname) {
            return pathname.getName().toUpperCase().endsWith(".ZIP");
        }
    };
    /**
     * Filtro para arquivos XML (*.XML)
     */
    public static final FileFilter FILE_FILTER_XML = new FileFilter() {
        public boolean accept(final File pathname) {
            return pathname.getName().toUpperCase().endsWith(".XML");
        }
    };

    private ZipUtils() {
    }

    /**
     * TODO - REFATORAR Seria interessante ir para um utilitário com ArquivoUtils Deleta todos os arquivos de um diretórios
     *
     * @throws IOException
     */
    public static void deletarArquivosDoDiretorio(final String diretorio) throws IOException {
        final File file = new File(diretorio);
        if (file.exists()) {
            if (file.isDirectory()) {
                for (final File fileInterno : file.listFiles()) {
                    fileInterno.delete();
                }
            } else {
                throw new IOException("Não é um diretório: [" + file.getAbsolutePath() + "]");
            }
        } else {
            throw new IOException("Diretório não existe: [" + file.getAbsolutePath() + "]");
        }
    }

    /**
     * Deleta um diretório e todos os seus arquivos
     *
     * @param diretorio
     *
     * @throws IOException
     *
     */
    public static void deletarDiretorio(final String diretorio) throws IOException {
        final File file = new File(diretorio);
        if (file.isDirectory()) {
            for (final File fileInterno : file.listFiles()) {
                fileInterno.delete();
            }
            file.delete();
        } else {
            throw new IOException("Não é um diretório: [" + file.getAbsolutePath() + "]");
        }
    }

    /**
     *
     * Descompacta o primeiro arquivo de um arquivo ZIP informado e retorna como Stream
     *
     * @param inputStream
     * @return ByteArrayOutputStream com o primeiro arquivo descompactado
     * @throws IOException
     */
    public static ByteArrayOutputStream descompactarPrimeiroArquivo(final InputStream inputStream)
            throws IOException {
        final ZipInputStream zin = new ZipInputStream(inputStream);

        ByteArrayOutputStream fout = null;

        try {
            if (zin.getNextEntry() != null) {
                fout = new ByteArrayOutputStream();

                for (int c = zin.read(); c != -1; c = zin.read()) {
                    fout.write(c);
                }
                zin.closeEntry();
                fout.close();
            }
            zin.close();
            return fout;
        } catch (final IOException e) {
            throw new IOException(e);
        }
    }

    /**
     *
     * Descompacta o primeiro arquivo de um arquivo ZIP informado e retorna como Stream
     *
     * @param InputStream
     * @return ByteArrayOutputStream com o primeiro arquivo descompactado
     * @throws IOException
     */
    public static InputStream descompactarPrimeiroArquivoToInputStream(final InputStream inputStream)
            throws IOException {
        try {
            final ZipInputStream zin = new ZipInputStream(inputStream);
            zin.getNextEntry();
            return zin;
        } catch (final IOException e) {
            throw new IOException(e);
        }
    }

    /**
     * @param inputStream
     * @param diretorioDestino
     * @return
     * @throws Exception
     */
    public static String descompactarPrimeiroArquivo(final InputStream inputStream, final String diretorioDestino)
            throws IOException {
        final ZipInputStream zin = new ZipInputStream(inputStream);
        final ZipEntry zipEntry;
        String nomeArquivo = STRING_EMPTY;

        FileOutputStream fout;
        try {
            zipEntry = zin.getNextEntry();
            if (zipEntry != null) {
                nomeArquivo = zipEntry.getName();
                final File diretorioSaida = new File(diretorioDestino).getCanonicalFile();
                final File arquivoSaida = new File(diretorioSaida, nomeArquivo).getCanonicalFile();

                if (!arquivoSaida.toPath().startsWith(diretorioSaida.toPath())) {
                    throw new IOException("Entrada ZIP inválida: " + nomeArquivo);
                }

                final File parent = arquivoSaida.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }

                fout = new FileOutputStream(arquivoSaida);

                for (int c = zin.read(); c != -1; c = zin.read()) {
                    fout.write(c);
                }
                zin.closeEntry();
                fout.close();
                nomeArquivo = arquivoSaida.getName();
            }
            zin.close();
            return nomeArquivo;
        } catch (final IOException e) {
            throw new IOException(e);
        }
    }

    /**
     *
     * Descompacta o primeiro arquivo de um arquivo ZIP informado e retorna como Stream
     *
     * @param inputStream
     * @return ByteArrayOutputStream com o primeiro arquivo descompactado
     * @throws IOException
     */
    public static String descompactar(final InputStream inputStream, final String diretorioDestino)
            throws IOException {
        final ZipInputStream zin = new ZipInputStream(inputStream);
        ZipEntry zipEntry = null;
        String nomeArquivo = STRING_EMPTY;
        final File diretorioSaida = new File(diretorioDestino).getCanonicalFile();

        FileOutputStream fout = null;
        try {
            while ((zipEntry = zin.getNextEntry()) != null) {
                nomeArquivo = zipEntry.getName();
                final File arquivoSaida = new File(diretorioSaida, nomeArquivo).getCanonicalFile();

                if (!arquivoSaida.toPath().startsWith(diretorioSaida.toPath())) {
                    throw new IOException("Entrada ZIP inválida: " + nomeArquivo);
                }

                final File parent = arquivoSaida.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }

                fout = new FileOutputStream(arquivoSaida);

                for (int c = zin.read(); c != -1; c = zin.read()) {
                    fout.write(c);
                }
                zin.closeEntry();
                fout.close();
            }
            zin.close();
            return nomeArquivo;
        } catch (final IOException e) {
            throw new IOException(e);
        }
    }

    /**
     *
     * Descompacta um arquivo ZIP para um diretório informado
     *
     * @param InputStream
     * @return ByteArrayOutputStream com o primeiro arquivo descompactado
     * @throws IOException
     */
    public static List<String> descompactar(final String nomZip, final String dirTemp) throws IOException {
        final List<String> resultado = new ArrayList<String>();
        int length = 0;
        final byte[] buffer = new byte[TAMANHO_BUFFER];
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(nomZip);
            final Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
            final File diretorioBase = new File(dirTemp).getCanonicalFile();
            final Path diretorioBaseNormalizado = diretorioBase.toPath().toAbsolutePath().normalize();

            try {
                while (enumeration.hasMoreElements()) {
                    final ZipEntry zipEntry = enumeration.nextElement();
                    final Path arquivoDestinoNormalizado = diretorioBaseNormalizado.resolve(zipEntry.getName()).normalize();
                    if (!arquivoDestinoNormalizado.startsWith(diretorioBaseNormalizado)) {
                        throw new IOException("Entrada de ZIP inválida: " + zipEntry.getName());
                    }
                    final File arquivoDestino = arquivoDestinoNormalizado.toFile();
                    final String arquivoDestinoPath = arquivoDestino.getPath();

                    if (zipEntry.isDirectory()) {
                        if (!arquivoDestino.exists() && !arquivoDestino.mkdirs()) {
                            throw new IOException("Não foi possível criar diretório: " + arquivoDestinoPath);
                        }
                        continue;
                    }

                    final File parent = arquivoDestino.getParentFile();
                    if (parent != null && !parent.exists() && !parent.mkdirs()) {
                        throw new IOException("Não foi possível criar diretório: " + parent.getPath());
                    }

                    final OutputStream outStream = new FileOutputStream(arquivoDestino);
                    final InputStream inStream = zipFile.getInputStream(zipEntry);
                    resultado.add(arquivoDestinoPath);
                    try {
                        while ((length = inStream.read(buffer)) != -1) {
                            outStream.write(buffer, 0, length);
                        }
                    } catch (final Exception e1) {
                        excluirArquivos(resultado);
                        resultado.clear();
                    } finally {
                        inStream.close();
                        outStream.close();
                    }
                }
            } catch (final Exception e2) {
                excluirArquivos(resultado);
                resultado.clear();
                throw new IOException(e2);
            } finally {
                zipFile.close();
            }

            return resultado;
        } catch (final IOException e) {
            throw new IOException(e);
        }

    }

    /**
     * Exclui uma lista de arquivos informadas
     *
     * @param arquivos
     * @return
     * @throws IOException
     */
    public static boolean excluirArquivos(final List<String> arquivos) throws IOException {
        if (arquivos == null || arquivos.size() == 0) {
            return true;
        } else {
            try {
                boolean resultado = true;
                if (arquivos.size() != 0) {
                    for (final String i : arquivos) {
                        if (!new File(i).delete()) {
                            resultado = false;
                        }
                    }
                }
                return resultado;
            } catch (final Exception e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * Compacata um arquivo no formato ZIP
     *
     * @param arquivo
     * @param nomeZip
     * @param descricao
     * @return
     * @throws Exception
     */
    public static boolean zipArquivo(final String arquivo, final String nomeZip, final String descricao)
            throws IOException {
        boolean resultado = false;
        final byte buffer[] = new byte[TAMANHO_BUFFER];

        try {
            final FileOutputStream fileOut = new FileOutputStream(nomeZip);
            final ZipOutputStream zipOutStream = new ZipOutputStream(fileOut);
            zipOutStream.setLevel(Deflater.BEST_COMPRESSION);
            final Adler32 adler = new Adler32();
            try {
                final String arq = arquivo;
                resultado = zipArquivosTemplate(descricao, 1, buffer, zipOutStream, adler, arq);
            } catch (final Exception e) {
                throw new IOException(e);
            } finally {
                zipOutStream.close();
                fileOut.close();
                // apaga o arquivo se não foi poss?vel gerar o .zip
                if (!resultado) {
                    new File(nomeZip).delete();
                }
            }
            return resultado;
        } catch (final Exception e) {
            throw new IOException(e);
        }
    }

    private static boolean zipArquivosTemplate(final String descricao, final int totArqs, final byte[] buffer,
            final ZipOutputStream zipOutStream, final Adler32 adler, final String arq) throws IOException {
        final boolean resultado;
        int length;
        final String arqNome = new File(arq).getName();
        InputStream inStream;
        try {
            inStream = new FileInputStream(arq);
            final ZipEntry zipEntry = new ZipEntry(arqNome);
            adler.update(zipEntry.getName().getBytes("UTF-8"));
            zipOutStream.putNextEntry(zipEntry);
            while ((length = inStream.read(buffer)) != -1) {
                zipOutStream.write(buffer, 0, length);
            }
            zipOutStream.closeEntry();
            inStream.close();
            adler.update(String.valueOf(zipEntry.getCrc()).getBytes());
            adler.update(totArqs);

            // ainda nao eh possivel ler o comentario principal do zip,
            // por isso eh adicionado o valor do adler no comentario de cada
            // arquivo, e a descricao, no caso de um arquivo so...
            zipEntry.setComment("1-" + adler.getValue() + "-" + descricao);
            resultado = true;
            // adiciona ao arquivo comentario com a "assinatura" ver
            // adlerZip
            zipOutStream.setComment(adler.getValue() + "-" + descricao);
            return resultado;
        } catch (final FileNotFoundException e) {
            throw new IOException("Arquivo não encontrado.", e);
        } catch (final UnsupportedEncodingException e) {
            throw new IOException("Encoding não suportado.", e);
        } catch (final IOException e) {
            throw new IOException(e);
        }
    }

    /**
     * Compacata uma lista de arquivos
     *
     *
     * @param arquivos
     * @param nomeZip
     * @param descricao
     * @return
     * @throws Exception
     */
    public static boolean zipArquivos(final List<String> arquivos, final String nomeZip, final String descricao) throws IOException {
        return zipArquivos(arquivos, nomeZip, descricao, false);
    }

    /**
     * Compacata uma lista de arquivos
     *
     * @param arquivos
     * @param nomeZip
     * @param descricao
     * @param semCompactar True se não é para utilizar a compactação.
     * @return
     * @throws IOException
     */
    public static boolean zipArquivos(final List<String> arquivos, final String nomeZip, final String descricao, final boolean semCompactar)
            throws IOException {
        boolean resultado = false;
        final byte buffer[] = new byte[TAMANHO_BUFFER];

        try {

            final FileOutputStream fileOut = new FileOutputStream(nomeZip);
            final ZipOutputStream zipOutStream = new ZipOutputStream(fileOut);

            zipOutStream.setLevel(semCompactar ? Deflater.NO_COMPRESSION : Deflater.BEST_COMPRESSION);
            final Adler32 adler = new Adler32();
            try {
                for (int i = 0; i < arquivos.size(); i++) {
                    final String arq = arquivos.get(i);
                    resultado = zipArquivosTemplate(descricao, arquivos.size(), buffer, zipOutStream, adler, arq);
                }

                resultado = true;
                // adiciona ao arquivo comentario com a "assinatura" ver
                // adlerZip
                zipOutStream.setComment(adler.getValue() + "-");
            } catch (final Exception e) {
                throw new IOException(e);
            } finally {
                zipOutStream.close();
                fileOut.close();
                // apaga o arquivo se não foi possivel gerar o .zip
                if (!resultado) {
                    new File(nomeZip).delete();
                }
            }
            return resultado;
        } catch (final Exception e) {
            throw new IOException(e);
        }
    }
    
    public static byte[] getBytes(File fZIP) throws IOException {
        FileInputStream fin = new FileInputStream(fZIP);
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        int bytesRead = 0;
        while ((bytesRead = fin.read(buffer)) != -1) {
            arrayOutputStream.write(buffer, 0, bytesRead);
        }
        arrayOutputStream.close();
        fin.close();
        return arrayOutputStream.toByteArray();
    }    
}
