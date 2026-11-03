import {spawnSync} from 'node:child_process'
import {existsSync, statSync} from 'node:fs'
import {createRequire} from 'node:module'
import {delimiter, dirname, isAbsolute, relative, resolve, sep} from 'node:path'
import {fileURLToPath} from 'node:url'

const root = resolve(dirname(fileURLToPath(import.meta.url)), '../..')
const modules = ['rfr-auth', 'rfr-api', 'rfr-e2e']

function paths(event) {
    const patch = typeof event.tool_input === 'string' ? event.tool_input : event.tool_input?.command
    if (typeof patch !== 'string') {
        throw new Error('Compilation hook requires the apply_patch command in tool_input')
    }
    return [...patch.matchAll(/^\*\*\* (?:Add File|Update File|Delete File|Move to): (.+)\r?$/gm)]
        .map(match => relative(root, resolve(event.cwd ?? root, match[1].trim())).split(sep).join('/'))
        .filter(path => path !== '..' && !path.startsWith('../') && !isAbsolute(path))
        .filter(path => !/(^|\/)(build|dist|node_modules|\.git)(\/|$)/.test(path))
}

function npm() {
    if (process.platform !== 'win32') {
        return 'npm'
    }
    const command = (process.env.PATH ?? '').split(delimiter).filter(Boolean)
        .map(directory => resolve(directory.replace(/^"|"$/g, ''), 'npm.cmd')).find(path => existsSync(path))
    if (!command) {
        throw new Error('Compilation hook cannot find npm.cmd in PATH')
    }
    return command
}
async function lint(files) {
    const directory = resolve(root, 'rfr-gql-client')
    const paths = [...new Set(files)]
        .filter(path => path.startsWith('rfr-gql-client/') && /\.(?:[cm]?[jt]s|[jt]sx)$/.test(path))
        .filter(path => existsSync(resolve(root, path)) && statSync(resolve(root, path)).isFile())
        .map(path => relative(directory, resolve(root, path)).split(sep).join('/'))
    if (paths.length === 0) {
        return []
    }
    const {ESLint} = createRequire(import.meta.url)(resolve(directory, 'node_modules/eslint'))
    const eslint = new ESLint({cwd: directory})
    const supported = []
    for (const path of paths) {
        if (await eslint.calculateConfigForFile(resolve(directory, path))) {
            supported.push(path)
        }
    }
    return supported.length === 0 ? [] : [{
        executable: process.execPath,
        arguments: [resolve(directory, 'node_modules/eslint/bin/eslint.js'), '--report-unused-disable-directives', '--max-warnings', '0', '--', ...supported],
        directory,
        shell: false,
    }]
}
async function checks(files) {
    const tasks = modules.flatMap(module => {
        const paths = files.filter(path => path.startsWith(`${module}/src/`) && path.endsWith('.java'))
        return paths.length === 0 ? [] : [`:${module}:${module === 'rfr-e2e' || paths.some(path => path.startsWith(`${module}/src/test/`)) ? 'testClasses' : 'classes'}`]
    })
    const java = tasks.length === 0 ? [] : [{
        executable: resolve(root, process.platform === 'win32' ? 'gradlew.bat' : 'gradlew'),
        arguments: ['--project-dir', root, '--console=plain', ...tasks],
        directory: root,
    }]
    const frontend = files.some(path => path.startsWith('rfr-gql-client/') && !path.endsWith('.md'))
    return [...java, ...(frontend ? [...await lint(files), {
        executable: npm(),
        arguments: ['run', 'build'],
        directory: resolve(root, 'rfr-gql-client'),
    }] : [])]
}

function run(check, deadline) {
    const command = [check.executable, ...check.arguments].map(argument => `"${argument}"`).join(' ')
    const timeout = deadline - Date.now()
    if (timeout <= 0) {
        return `${command}\nFAILED: Compilation hook exceeded its 270-second budget`
    }
    const result = spawnSync(check.shell === false ? check.executable : command, check.shell === false ? check.arguments : [], {
        cwd: check.directory,
        shell: check.shell ?? true,
        encoding: 'utf8',
        timeout,
        maxBuffer: 8 * 1024 * 1024,
        windowsHide: true,
        stdio: ['ignore', 'pipe', 'pipe'],
    })
    return [
        `$ ${command}`,
        `Working directory: ${check.directory}`,
        `Exit code: ${result.status ?? 'unavailable'}`,
        result.stdout,
        result.stderr,
        ...(result.error ? [`FAILED: ${result.error.message}`] : []),
        ...(result.signal ? [`Terminated by signal: ${result.signal}`] : []),
    ].filter(Boolean).join('\n')
}

function feedback(context) {
    process.stdout.write(JSON.stringify({
        hookSpecificOutput: {
            hookEventName: 'PostToolUse',
            additionalContext: context,
        },
    }) + '\n')
}

try {
    let input = ''
    for await (const chunk of process.stdin) {
        input += chunk
    }
    const event = JSON.parse(input)
    if (event.hook_event_name !== 'PostToolUse' || !['apply_patch', 'Edit', 'Write'].includes(event.tool_name)) {
        throw new Error('Compilation hook received an unsupported event or tool')
    }
    const files = paths(event)
    const commands = await checks(files)
    if (commands.length > 0) {
        const deadline = Date.now() + 270_000
        feedback(`Compilation checks after editing ${files.join(', ')}\n\n${commands.map(check => run(check, deadline)).join('\n\n')}`)
    }
} catch (error) {
    feedback(`Compilation hook failed: ${error.stack ?? error.message}`)
}
